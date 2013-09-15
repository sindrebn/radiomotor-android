package no.radiomotor.android;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.OnActivityResult;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static no.radiomotor.android.RadiomotorXmlParser.Item;

@EActivity(R.layout.main)
@OptionsMenu(R.menu.main)
public class MyActivity extends Activity {

	@ViewById ListView newsFeedList;
	@ViewById TextView noNewsTextView;

    private final String IMAGE_PATH = Environment.getExternalStorageDirectory()+File.separator + "radiomotor.jpg";
    private final int PICTURE_REQUEST_CODE = 1;
	private CacheHelper cacheHelper;

	MenuItem refresh;

	@AfterViews
	void getRss() {
		cacheHelper = new CacheHelper(getApplicationContext());
		updateListview();
		downloadNewsfeed("http://www.radiomotor.no/feed/");
	}

    @OptionsItem(R.id.action_picture)
    public void cameraSelected() {
        File file = new File(IMAGE_PATH);

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));

        this.startActivityForResult(cameraIntent, PICTURE_REQUEST_CODE);
    }

    @OptionsItem(R.id.action_play)
    public void radioPlayerSelected() {
    }

	@OptionsItem(R.id.action_refresh)
	public void newsRefreshSelected() {
		refresh.setActionView(R.layout.actionbar_indeterminate_progress);
		downloadNewsfeed("http://www.radiomotor.no/feed/");
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		refresh = menu.findItem(R.id.action_refresh);
		return true;
	}

    @OnActivityResult(PICTURE_REQUEST_CODE)
    void onResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Fragment prev = getFragmentManager().findFragmentByTag("image");
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (prev != null) {
                ft.remove(prev);
            }
            ImageFragment imageFragment = new ImageFragment();
            imageFragment.show(ft, "image");
        }
    }

	@Background
	void downloadNewsfeed(String urlString) {
		InputStream stream = null;
		RadiomotorXmlParser parser = new RadiomotorXmlParser();
		ArrayList<Item> entries;
		try {
			stream = downloadUrl(urlString);
			entries = parser.parse(stream);
			cacheHelper.writeNewsItems(entries);
		} catch (XmlPullParserException e) {
			errorMessage(R.string.parse_error);
		} catch (IOException e) {
			errorMessage(R.string.connection_error);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {}
			}
		}
		updateListview();
	}

	@UiThread
	void updateListview() {
		if (refresh != null) {
			refresh.setActionView(null);
		}
		ArrayList<Item> items = new ArrayList<Item>();
		try {
			items = cacheHelper.readNewsItems();
		} catch (IOException e) {
			Log.e("SBN", e.getLocalizedMessage(), e);
		}

		if (items.size() > 0) {
			noNewsTextView.setVisibility(View.GONE);
			newsFeedList.setAdapter(new NewsFeedAdapter(getApplicationContext(), R.layout.row_newsitem, items));
		} else {
			newsFeedList.setVisibility(View.GONE);
			noNewsTextView.setVisibility(View.VISIBLE);
		}
	}

	@UiThread
	void errorMessage(int resourceId) {
		Toast.makeText(getApplicationContext(), resourceId, Toast.LENGTH_LONG).show();
	}

	@ItemClick
	void newsFeedListItemClicked(Item item) {
		NewsItemActivity_.intent(getApplicationContext()).flags(FLAG_ACTIVITY_NEW_TASK).newsItem(item).start();
	}

	private InputStream downloadUrl(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
		conn.connect();
		return conn.getInputStream();
	}
}
