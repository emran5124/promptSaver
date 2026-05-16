package com.promptvault;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PromptAdapter.OnPromptClickListener {

    private static final int REQ_STORAGE_PERM = 100;

    private PromptDatabase db;
    private PromptAdapter adapter;
    private RecyclerView recyclerView;
    private EditText etSearch;
    private Spinner spinnerFilter;
    private View emptyState;

    private String currentFilter = "all";
    private String currentSearch = "";

    private final ActivityResultLauncher<Intent> addEditLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) loadPrompts();
            });

    private final ActivityResultLauncher<Intent> filePicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    JsonSyncManager.syncFromUri(this, uri, new JsonSyncManager.SyncCallback() {
                        @Override
                        public void onSuccess(int imported, int updated) {
                            setupFilterSpinner();
                            loadPrompts();
                            String msg = "وارد شد: " + imported + " جدید، " + updated + " بروزرسانی";
                            Snackbar.make(recyclerView, msg, Snackbar.LENGTH_LONG).show();
                        }
                        @Override
                        public void onError(String message) {
                            Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        db = PromptDatabase.getInstance(this);

        recyclerView = findViewById(R.id.recycler_view);
        etSearch = findViewById(R.id.et_search);
        spinnerFilter = findViewById(R.id.spinner_filter);
        emptyState = findViewById(R.id.empty_state);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        adapter = new PromptAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                currentSearch = s.toString().trim();
                loadPrompts();
            }
        });

        fab.setOnClickListener(v -> {
            Intent i = new Intent(this, AddEditPromptActivity.class);
            addEditLauncher.launch(i);
        });

        setupFilterSpinner();
        loadPrompts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupFilterSpinner();
        loadPrompts();
    }

    private void setupFilterSpinner() {
        List<String> items = new ArrayList<>();
        items.add("همه");
        items.add("★ موردعلاقه");
        items.addAll(db.getAllCategories());

        ArrayAdapter<String> arr = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        arr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(arr);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos == 0) currentFilter = "all";
                else if (pos == 1) currentFilter = "favorites";
                else currentFilter = items.get(pos);
                loadPrompts();
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadPrompts() {
        List<Prompt> list;
        if (!currentSearch.isEmpty()) {
            list = db.search(currentSearch);
        } else if ("favorites".equals(currentFilter)) {
            list = db.getFavorites();
        } else if ("all".equals(currentFilter)) {
            list = db.getAll();
        } else {
            list = db.getByCategory(currentFilter);
        }

        adapter.setPrompts(list);
        emptyState.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onView(Prompt prompt) {
        Intent i = new Intent(this, ViewPromptActivity.class);
        i.putExtra("prompt_id", prompt.getId());
        startActivity(i);
    }

    @Override
    public void onEdit(Prompt prompt) {
        Intent i = new Intent(this, AddEditPromptActivity.class);
        i.putExtra("prompt_id", prompt.getId());
        addEditLauncher.launch(i);
    }

    @Override
    public void onDelete(Prompt prompt) {
        new AlertDialog.Builder(this)
                .setTitle("حذف پرامپت")
                .setMessage("آیا مطمئنید که می‌خواهید «" + prompt.getTitle() + "» را حذف کنید؟")
                .setPositiveButton("حذف", (d, w) -> {
                    db.delete(prompt.getId());
                    loadPrompts();
                    Toast.makeText(this, "حذف شد", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("انصراف", null)
                .show();
    }

    @Override
    public void onFavoriteToggle(Prompt prompt) {
        prompt.setFavorite(!prompt.isFavorite());
        db.insertOrUpdate(prompt);
        loadPrompts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_sync_downloads) {
            syncFromDownloads();
            return true;
        } else if (id == R.id.menu_import_file) {
            openFilePicker();
            return true;
        } else if (id == R.id.menu_about) {
            showAbout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void syncFromDownloads() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_STORAGE_PERM);
                return;
            }
        }
        doSync();
    }

    private void doSync() {
        JsonSyncManager.syncFromDownloads(this, new JsonSyncManager.SyncCallback() {
            @Override
            public void onSuccess(int imported, int updated) {
                setupFilterSpinner();
                loadPrompts();
                String msg = "همگام‌سازی موفق: " + imported + " جدید، " + updated + " بروزرسانی";
                Snackbar.make(recyclerView, msg, Snackbar.LENGTH_LONG).show();
            }
            @Override
            public void onError(String message) {
                Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void openFilePicker() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        filePicker.launch(Intent.createChooser(i, "انتخاب فایل JSON"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQ_STORAGE_PERM) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                doSync();
            } else {
                Toast.makeText(this, "دسترسی به حافظه لازم است", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAbout() {
        new AlertDialog.Builder(this)
                .setTitle("PromptVault")
                .setMessage("نسخه ۱.۰\n\nمدیریت و ذخیره‌سازی پرامپت‌های هوش مصنوعی\n\nبرای همگام‌سازی، فایل prompts.json را در پوشه Downloads قرار دهید.")
                .setPositiveButton("باشه", null)
                .show();
    }
}