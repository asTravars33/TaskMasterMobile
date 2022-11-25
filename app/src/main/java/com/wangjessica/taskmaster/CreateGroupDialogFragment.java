package com.wangjessica.taskmaster;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import yuku.ambilwarna.AmbilWarnaDialog;

public class CreateGroupDialogFragment extends DialogFragment {

    CreateGroupListener listener;

    // Color picking
    int pickedColor = 0;
    View colorPreview;

    public interface CreateGroupListener{
        public void onDialogPositiveClick(String title, int capacity, int color);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (CreateGroupListener) context;
    }
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set a custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.create_group_form, null);
        builder.setView(customLayout);
        // Color selector
        Button colorButton = customLayout.findViewById(R.id.color_button);
        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openColorPicker();
            }
        });
        colorPreview = customLayout.findViewById(R.id.color);
        // Create the AlertDialog object and return it
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String curTitle = ((EditText) customLayout.findViewById(R.id.name)).getText().toString();
                int capacity = Integer.parseInt(((EditText) customLayout.findViewById(R.id.capacity)).getText().toString());
                listener.onDialogPositiveClick(curTitle, capacity, pickedColor); // Group: Let the user enter tags
            }
        });
        return builder.create();
    }
    public void openColorPicker(){
        final AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this.getContext(), 0, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                pickedColor = color;
                colorPreview.setBackgroundColor(pickedColor);
            }
        });
        colorPicker.show();
    }
}