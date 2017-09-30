package com.ibm.visual_recognition;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.DetectedFaces;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.Face;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ImageClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ImageFace;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier;

import java.util.List;
import java.util.Locale;

/**
 * Class used to construct a UI to deliver information received from Visual Recognition to the user.
 */
class RecognitionResultBuilder {

    private final MainActivity context;

    RecognitionResultBuilder(MainActivity context) {
        this.context = context;
    }

    /**
     * Dynamically constructs a LinearLayout with information from Visual Recognition.
     * @return A LinearLayout with a dynamic number image_tag.xml
     */
    LinearLayout buildRecognitionResultView(VisualClassification visualClassification, DetectedFaces detectedFaces) {
        LinearLayout recognitionLayout = new LinearLayout(context);
        recognitionLayout.setOrientation(LinearLayout.VERTICAL);

        FlexboxLayout imageTagContainer = (FlexboxLayout)context.getLayoutInflater().inflate(R.layout.tag_box, null);

        // First process facial data from Visual Recognition. For each feature create an image tag with a name and score.
        List<ImageFace> potentialFaces = detectedFaces.getImages();
        for (int i = 0; i < potentialFaces.size(); i++) {
            List<Face> allFaces = potentialFaces.get(i).getFaces();
            if (allFaces == null) {break;}
            for (Face face : allFaces) {
                if (face.getIdentity() != null) {
                    String formattedScore = String.format(Locale.US, "%.0f", face.getIdentity().getScore() * 100) + "%";
                    imageTagContainer.addView(constructImageTag(context.getLayoutInflater(),
                            face.getIdentity().getName(), formattedScore));
                }

                String faceResult = "";
                String faceScore = "";

                Face.Gender gender = face.getGender();
                if (gender.getGender() != null) {
                    faceResult += gender.getGender();
                    faceScore += String.format(Locale.US, "%.0f", gender.getScore() * 100) + "%";
                } else {
                    faceResult += "Unknown Gender";
                    faceScore += "N/A";
                }

                Face.Age age = face.getAge();
                if (age != null) {
                    if (age.getMin() == null) {age.setMin(0);}
                    if (age.getMax() == null) {age.setMax(age.getMin()+15);}
                    faceResult += " (" + age.getMin() + " - " + age.getMax() + ")";
                    faceScore += " (" + String.format(Locale.US, "%.0f", age.getScore() * 100) + "%)";
                }
                imageTagContainer.addView(constructImageTag(context.getLayoutInflater(), faceResult, faceScore));
            }
        }

        // Next process general classification data from Visual Recognition and create image tags for each visual class.
        List<ImageClassification> classifications = visualClassification.getImages();

        for (int i = 0; i < classifications.size(); i++) {
            List<VisualClassifier> classifiers = classifications.get(i).getClassifiers();
            if (classifiers == null) break;
            for (int j = 0; j < classifiers.size(); j++) {
                List<VisualClassifier.VisualClass> visualClasses = classifiers.get(j).getClasses();
                if (visualClasses == null) break;
                for (VisualClassifier.VisualClass visualClass : visualClasses) {
                    String formattedScore = String.format(Locale.US, "%.0f", visualClass.getScore() * 100) + "%";
                    imageTagContainer.addView(constructImageTag(context.getLayoutInflater(), visualClass.getName(), formattedScore));
                }
            }
        }

        // If parsing through Visual Recognition's return has resulted in no image tags, create an "Unknown" tag.
        if (imageTagContainer.getChildCount() <= 0) {
            imageTagContainer.addView(constructImageTag(context.getLayoutInflater(), "Unknown", "N/A"));
        }

        recognitionLayout.addView(imageTagContainer);

        return recognitionLayout;
    }

    /**
     * Creates a TextView image tag with a name and score to be displayed to the user.
     * @param inflater Layout inflater to access R.layout.image_tag.
     * @param tagName Name of the tag to be displayed.
     * @param tagScore Certainty score of the tag, to be displayed when the user clicks the tag.
     * @return A TextView representation of the image tag.
     */
    static TextView constructImageTag(LayoutInflater inflater, final String tagName, final String tagScore) {
        TextView imageTagView = (TextView)inflater.inflate(R.layout.image_tag, null);
        imageTagView.setText(tagName);

        // Set an onclick listener that gives each image tag a toggle between its name and its score.
        imageTagView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView label = (TextView)v;
                String currentText = label.getText().toString();

                if (currentText.equals(tagName)) {
                    label.setMinWidth(label.getWidth());
                    label.setText(tagScore);
                } else {
                    label.setText(tagName);
                }
            }
        });

        return imageTagView;
    }
}
