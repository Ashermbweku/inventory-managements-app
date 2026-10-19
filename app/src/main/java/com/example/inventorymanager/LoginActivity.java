package com.example.inventorymanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inventorymanager.database.InventoryDatabase;
import com.example.inventorymanager.database.User;
import com.example.inventorymanager.database.UserDao;
import com.example.inventorymanager.databinding.ActivityLoginBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private UserDao userDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userDao = InventoryDatabase.getInstance(this).userDao();

        // Seed default admin user if not present
        executorService.execute(() -> {
            if (userDao.getAllUsers().isEmpty()) {
                userDao.insert(new User("admin", "admin", "ADMIN"));
            }
        });

        binding.buttonLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String username = binding.editTextUsername.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            User user = userDao.login(username, password);
            runOnUiThread(() -> {
                if (user != null) {
                    // Store user session (simplified for now)
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("USER_ROLE", user.getRole());
                    intent.putExtra("USERNAME", user.getUsername());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
