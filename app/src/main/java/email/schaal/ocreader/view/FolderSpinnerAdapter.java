package email.schaal.ocreader.view;

import android.content.Context;
import androidx.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import email.schaal.ocreader.R;
import email.schaal.ocreader.database.model.Folder;
import io.realm.OrderedRealmCollection;

/**
 * Adapter for Spinner to display Folders
 */
public class FolderSpinnerAdapter extends BaseAdapter {

    private final static int VIEW_TYPE_NONE = 0;
    private final static int VIEW_TYPE_FOLDER = 1;

    private final String rootFolder;
    private final OrderedRealmCollection<Folder> folders;

    public FolderSpinnerAdapter(Context context, OrderedRealmCollection<Folder> folders) {
        this.rootFolder = context.getString(R.string.root_folder);
        this.folders = folders;
    }

    @Override
    public int getCount() {
        return folders.size()+1;
    }

    @Override
    public Object getItem(int position) {
        if(position == 0)
            return rootFolder;
        return folders.get(position-1).getName();
    }

    @Override
    public long getItemId(int position) {
        if(position == 0)
            return 0;
        return folders.get(position-1).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_NONE : VIEW_TYPE_FOLDER;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getSpinnerView(position, convertView, parent, R.layout.spinner_folder_dropdown);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getSpinnerView(position, convertView, parent, R.layout.spinner_folder);
    }

    private View getSpinnerView(int position, View convertView, ViewGroup parent, @LayoutRes int layout) {
        View view = convertView != null ? convertView : View.inflate(parent.getContext(), layout, null);
        ((TextView)view).setText((String)getItem(position));
        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public int getPosition(long folderId) {
        if(folderId == 0)
            return 0;

        for (int i = 1, foldersSize = folders.size(); i-1 < foldersSize; i++) {
            if(folders.get(i-1).getId() == folderId) {
                return i;
            }
        }
        return -1;
    }
}
