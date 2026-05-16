package com.promptvault;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class AddEditPromptActivity extends AppCompatActivity {

    private EditText etTitle, etContent, etCategory, etTags;
    private AutoCompleteTextView etModel;
    private PromptDatabase db;
    private Prompt existingPrompt;

    private static final String[] AI_MODELS = {
            "ChatGPT", "GPT-4", "GPT-4o", "Claude", "Claude 3", "Claude 3.5 Sonnet",
            "Gemini", "Gemini Pro", "Gemini Ultra", "Llama", "Llama 3",
            "Mistral", "Mixtral", "Copilot", "Grok", "Perplexity",
            "Stable Diffusion", "Midjourney", "DALL-E", "General"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_prompt);

        db = PromptDatabase.getInstance(this);

        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        etCategory = findViewById(R.id.et_category);
        etTags = findViewById(R.id.et_tags);
        etModel = findViewById(R.id.et_model);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnCancel = findViewById(R.id.btn_cancel);

        // Model autocomplete
        ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, AI_MODELS);
        etModel.setAdapter(modelAdapter);
        etModel.setThreshold(1);

        // Category autocomplete suggestions from DB
        List<String> cats = db.getAllCategories();
        AutoCompleteTextView acCategory = (AutoCompleteTextView) etCategory;
        if (etCategory instanceof AutoCompleteTextView) {
            ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, cats);
            ((AutoCompleteTextView) etCategory).setAdapter(catAdapter);
        }

        // Load existing if editing
        String promptId = getIntent().getStringExtra("prompt_id");
        if (promptId != null) {
            existingPrompt = db.getById(promptId);
            if (existingPrompt != null) {
                etTitle.setText(existingPrompt.getTitle());
                etContent.setText(existingPrompt.getContent());
                etCategory.setText(existingPrompt.getCategory());
                etTags.setText(existingPrompt.getTags());
                etModel.setText(existingPrompt.getAiModel());
                getSupportActionBar();
                setTitle("ویرایش پرامپت");
            }
        } else {
            setTitle("پرامپت جدید");
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        btnSave.setOnClickListener(v -> save());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void save() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String tags = etTags.getText().toString().trim();
        String model = etModel.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("عنوان الزامی است");
            etTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(content)) {
            etContent.setError("محتوای پرامپت الزامی است");
            etContent.requestFocus();
            return;
        }

        if (existingPrompt != null) {
            existingPrompt.setTitle(title);
            existingPrompt.setContent(content);
            existingPrompt.setCategory(category);
            existingPrompt.setTags(tags);
            existingPrompt.setAiModel(model);
            db.insertOrUpdate(existingPrompt);
            Toast.makeText(this, "بروزرسانی شد", Toast.LENGTH_SHORT).show();
        } else {
            Prompt p = new Prompt(title, content, category, tags, model);
            db.insertOrUpdate(p);
            Toast.makeText(this, "ذخیره شد", Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
