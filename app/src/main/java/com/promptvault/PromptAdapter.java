package com.promptvault;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PromptAdapter extends RecyclerView.Adapter<PromptAdapter.ViewHolder> {

    public interface OnPromptClickListener {
        void onView(Prompt prompt);
        void onEdit(Prompt prompt);
        void onDelete(Prompt prompt);
        void onFavoriteToggle(Prompt prompt);
    }

    private List<Prompt> prompts = new ArrayList<>();
    private final OnPromptClickListener listener;

    public PromptAdapter(OnPromptClickListener listener) {
        this.listener = listener;
    }

    public void setPrompts(List<Prompt> list) {
        this.prompts = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prompt, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Prompt p = prompts.get(position);
        h.tvTitle.setText(p.getTitle());
        h.tvPreview.setText(p.getContent());
        h.tvModel.setText(p.getAiModel() != null && !p.getAiModel().isEmpty() ? p.getAiModel() : "General");
        h.tvCategory.setText(p.getCategory() != null && !p.getCategory().isEmpty() ? p.getCategory() : "");
        h.tvCategory.setVisibility(p.getCategory() != null && !p.getCategory().isEmpty() ? View.VISIBLE : View.GONE);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        h.tvDate.setText(sdf.format(new Date(p.getUpdatedAt())));

        h.btnFavorite.setImageResource(p.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);

        h.itemView.setOnClickListener(v -> listener.onView(p));
        h.btnEdit.setOnClickListener(v -> listener.onEdit(p));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(p));
        h.btnFavorite.setOnClickListener(v -> listener.onFavoriteToggle(p));
        h.btnCopy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("prompt", p.getContent()));
            Toast.makeText(v.getContext(), "کپی شد", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return prompts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPreview, tvModel, tvCategory, tvDate;
        ImageButton btnEdit, btnDelete, btnFavorite, btnCopy;

        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_title);
            tvPreview = v.findViewById(R.id.tv_preview);
            tvModel = v.findViewById(R.id.tv_model);
            tvCategory = v.findViewById(R.id.tv_category);
            tvDate = v.findViewById(R.id.tv_date);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
            btnFavorite = v.findViewById(R.id.btn_favorite);
            btnCopy = v.findViewById(R.id.btn_copy);
        }
    }
}
