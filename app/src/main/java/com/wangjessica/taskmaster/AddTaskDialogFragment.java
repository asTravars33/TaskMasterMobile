package com.wangjessica.taskmaster;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class AddTaskDialogFragment extends DialogFragment{
    AddTaskListener listener;

    public interface AddTaskListener{
        public void onDialogPositiveClick(String name, double time);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (AddTaskListener) context;
    }
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set a custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.add_task_form, null);
        builder.setView(customLayout);
        // Create the AlertDialog object and return it
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String name = ((EditText) customLayout.findViewById(R.id.task_name)).getText().toString();
                Double time = Double.parseDouble(((EditText) customLayout.findViewById(R.id.task_length)).getText().toString());
                listener.onDialogPositiveClick(name, time);
            }
        });
        return builder.create();
    }
}
