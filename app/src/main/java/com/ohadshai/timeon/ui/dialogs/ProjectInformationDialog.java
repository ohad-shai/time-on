package com.ohadshai.timeon.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ohadshai.timeon.R;
import com.ohadshai.timeon.entities.Project;
import com.ohadshai.timeon.ui.UIConsts;
import com.ohadshai.timeon.utils.Utils;

/**
 * Represents a dialog for showing the "Project More Information".
 * Created by Ohad on 03/03/2017.
 */
public class ProjectInformationDialog extends DialogFragment {

    //region Private Members

    /**
     * Holds the project object to show the information.
     */
    private Project _project;

    //endregion

    //region Dialog Events

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_project_information, null);

        this.initControls(dialogView);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        return builder.create();
    }

    //endregion

    //region Public Static API

    /**
     * Initializes a new instance of the dialog, and shows it.
     *
     * @param manager The fragment manager to show the dialog in.
     * @param project The project object to show the information.
     * @return Returns the initialized dialog.
     */
    public static ProjectInformationDialog show(@NonNull FragmentManager manager, Project project) {
        ProjectInformationDialog dialog = new ProjectInformationDialog();

        Bundle bundle = new Bundle();
        bundle.putParcelable(UIConsts.Bundles.PROJECT_KEY, project);
        dialog.setArguments(bundle);

        dialog.show(manager, UIConsts.Fragments.PROJECT_MORE_INFO_DIALOG_TAG);
        return dialog;
    }

    //endregion

    //region Private Methods

    /**
     * Initializes all dialog view controls.
     *
     * @param view The view of the dialog.
     */
    @SuppressLint("DefaultLocale")
    private void initControls(final View view) {
        if (getArguments() == null)
            throw new NullPointerException("dialog: getArguments");

        // Gets the provided project object:
        _project = getArguments().getParcelable(UIConsts.Bundles.PROJECT_KEY);
        if (_project == null)
            throw new NullPointerException("_project");

        final ImageButton imgbtnClose = (ImageButton) view.findViewById(R.id.imgbtnClose);
        imgbtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        imgbtnClose.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Utils.UI.showInformationToast(imgbtnClose, R.string.general_btn_close);
                return true;
            }
        });

        ((TextView) view.findViewById(R.id.lblProjectName)).setText(_project.getName());

        TextView lblProjectDescription = (TextView) view.findViewById(R.id.lblProjectDescription);
        if (_project.getDescription() == null || _project.getDescription().trim().equals(""))
            lblProjectDescription.setText(R.string.project_description_empty);
        else
            lblProjectDescription.setText(_project.getDescription());

        TextView lblProjectCreateDate = (TextView) view.findViewById(R.id.lblProjectCreateDate);
        lblProjectCreateDate.setText(String.format("%1$tA, %1$tb %1$td, %1$tY at %1$tI:%1$tM %1$Tp", _project.getCreateDate()));

    }

    //endregion

}
