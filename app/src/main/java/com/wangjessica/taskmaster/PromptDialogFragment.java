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

public class PromptDialogFragment extends DialogFragment {

    StartQuestListener listener;

    public interface StartQuestListener{
        public void onDialogPositiveClick(String prompt);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (StartQuestListener) context;
    }
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set a custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.input_prompt_form, null);
        builder.setView(customLayout);
        // Random prompt button
        Button randomButton = customLayout.findViewById(R.id.gen_random);
        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Do a random prompt
                String[] prompts = new String[] {"You begin your quest to slay a dragon. ", "You begin your quest to find a diamond. "};
                String curPrompt = prompts[(int)(Math.random()*prompts.length)];
                listener.onDialogPositiveClick(curPrompt);
            }
        });
        // Create the AlertDialog object and return it
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String curPrompt = ((EditText) customLayout.findViewById(R.id.edit_prompt)).getText().toString();
                listener.onDialogPositiveClick(curPrompt);
            }
        });
        return builder.create();
    }
}