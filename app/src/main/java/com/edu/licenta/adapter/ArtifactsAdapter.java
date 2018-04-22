package com.edu.licenta.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.delta.activities.R;
import com.edu.licenta.model.Artifact;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by naritc
 * on 19-Apr-18.
 */

public class ArtifactsAdapter extends ArrayAdapter<Artifact> {

    private Context context;
    private int layoutResourceId;
    private List<Artifact> artifacts = new ArrayList<>();

    public ArtifactsAdapter(@NonNull Context context, int resource, @NonNull List<Artifact> objects) {
        super(context, resource, objects);

        this.context =  context;
        this.layoutResourceId = resource;
        this.artifacts = objects;
    }

    @Nullable
    @Override
    public Artifact getItem(int position) {
        return super.getItem(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        ArtifactHolder holder;

        if(row == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ArtifactHolder();
            holder.name = row.findViewById(R.id.artifactName);
            holder.tagId = row.findViewById(R.id.artifactTagId);

            row.setTag(holder);
        } else {
            holder = (ArtifactHolder) row.getTag();
        }

        Artifact artifact = artifacts.get(position);

        holder.name.setText(artifact.getName());
        holder.tagId.setText(artifact.getTagId());

        return row;
    }

    private static class ArtifactHolder {
        TextView name;
        TextView tagId;
    }
}
