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

public class CreateTodoDialogFragment extends DialogFragment {

    CreateTodoListener listener;

    // Color picking
    int pickedColor = 0;
    View colorPreview;

    public interface CreateTodoListener{
        public void onDialogPositiveClick(String title, String date, int color, ArrayList<String> tags);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (CreateTodoListener) context;
    }
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set a custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.create_todo_form, null);
        // Get the date and modify the custom layout's hint
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
        ((EditText) customLayout.findViewById(R.id.date)).setText(dateFormat.format(date));
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
                // Get the info
                String curTitle = ((EditText) customLayout.findViewById(R.id.name)).getText().toString();
                String curDate = ((EditText) customLayout.findViewById(R.id.date)).getText().toString();
                String curTags = ((EditText) customLayout.findViewById(R.id.tags)).getText().toString();
                // Create tags array
                String[] tags = curTags.split(",\\s*");
                ArrayList<String> tagsList = new ArrayList<String>();
                for(String tag: tags){
                    tagsList.add(tag);
                }
                // Create the to-do object
                listener.onDialogPositiveClick(curTitle, curDate, pickedColor, tagsList);
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