package com.ibm.visual_recognition;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.flexbox.FlexboxLayout;

/**
 * Holds and retains the Analyzer Result Data UI from RecognitionResultBuilder
 */
public class RecognitionResultFragment extends Fragment {

    private LinearLayout retainedLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Check if we've retained the Fragment or are creating it for the first time.
        if (retainedLayout == null) {
            // When creating for the first time we need to add the default tags for the default image.
            LinearLayout resultView = (LinearLayout)inflater.inflate(R.layout.recognition_result_view, container, false);
            FlexboxLayout imageTagContainer = (FlexboxLayout)inflater.inflate(R.layout.tag_box, null, false);
            imageTagContainer.addView(RecognitionResultBuilder.constructImageTag(inflater, "Blue Sky", "85%"));
            imageTagContainer.addView((RecognitionResultBuilder.constructImageTag(inflater, "Landscape", "60%")));
            resultView.addView(imageTagContainer);

            return resultView;
        } else {
            return retainedLayout;
        }
    }

    /**
     * Called by MainActivity.onDestroy to save the state of the fragment UI.
     */
    public void saveData() {
        retainedLayout = (LinearLayout) getView();
    }
}