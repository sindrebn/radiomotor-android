package no.radiomotor.android;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;

public class ImageFragment extends DialogFragment {
    private final String IMAGE_PATH = Environment.getExternalStorageDirectory()+ File.separator + "radiomotor.jpg";
    ImageView imageView;
    ImageButton uploadButton;

    public ImageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_fragment, container, false);
        uploadButton = (ImageButton) view.findViewById(R.id.uploadButton);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setImageURI(Uri.parse(IMAGE_PATH));
        imageView.invalidate();

        return view;
    }
}
