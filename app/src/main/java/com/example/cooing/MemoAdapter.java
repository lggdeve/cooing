package com.example.cooing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.MemoViewHolder> {

    private List<String> memoList;

    public MemoAdapter(List<String> memoList) {
        this.memoList = memoList;
    }

    @NonNull
    @Override
    public MemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new MemoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoViewHolder holder, int position) {
        holder.memoTextView.setText(memoList.get(position));
    }

    @Override
    public int getItemCount() {
        return memoList.size();
    }

    public void updateData(List<String> newMemoList) {
        this.memoList = newMemoList;
        notifyDataSetChanged();
    }

    public static class MemoViewHolder extends RecyclerView.ViewHolder {
        TextView memoTextView;

        public MemoViewHolder(@NonNull View itemView) {
            super(itemView);
            memoTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}