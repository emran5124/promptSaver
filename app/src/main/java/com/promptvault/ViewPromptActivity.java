package com.promptvault;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ViewPromptActivity extends AppCompatActivity {

    private Prompt prompt;
    private PromptDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_prompt);

        db = PromptDatabase.getInstance(this);

        String id = getIntent().getStringExtra("prompt_id");
        if (id == null) { finish(); return; }
        prompt = db.getById(id);
        if (prompt == null) { finish(); return; }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(prompt.getTitle());
        }

        TextView tvContent = findViewById(R.id.tv_content);
        TextView tvModel = findViewById(R.id.tv_model);
        TextView tvCategory = findViewById(R.id.tv_category);
        TextView tvTags = findViewById(R.id.tv_tags);
        TextView tvDate = findViewById(R.id.tv_date);

        tvContent.setText(prompt.getContent());
        tvModel.setText("مدل: " + (prompt.getAiModel() != null && !prompt.getAiModel().isEmpty() ? prompt.getAiModel() : "—"));
        tvCategory.setText("دسته‌بندی: " + (prompt.getCategory() != null && !prompt.getCategory().isEmpty() ? prompt.getCategory() : "—"));
        tvTags.setText("تگ‌ها: " + (prompt.getTags() != null && !prompt.getTags().isEmpty() ? prompt.getTags() : "—"));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        tvDate.setText("آخرین ویرایش: " + sdf.format(new Date(prompt.getUpdatedAt())));

        findViewById(R.id.btn_copy).setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("prompt", prompt.getContent()));
            Toast.makeText(this, "کپی شد!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_menu, menu);
        MenuItem favItem = menu.findItem(R.id.menu_favorite);
        favItem.setIcon(prompt.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_edit) {
            Intent i = new Intent(this, AddEditPromptActivity.class);
            i.putExtra("prompt_id", prompt.getId());
            startActivity(i);
            return true;
        } else if (id == R.id.menu_favorite) {
            prompt.setFavorite(!prompt.isFavorite());
            db.insertOrUpdate(prompt);
            item.setIcon(prompt.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
            Toast.makeText(this, prompt.isFavorite() ? "به موردعلاقه‌ها اضافه شد" : "از موردعلاقه‌ها حذف شد", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload in case we just edited
        if (prompt != null) {
            prompt = db.getById(prompt.getId());
            if (prompt != null) {
                ((TextView) findViewById(R.id.tv_content)).setText(prompt.getContent());
                if (getSupportActionBar() != null) getSupportActionBar().setTitle(prompt.getTitle());
            }
        }
    }
}
