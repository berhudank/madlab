package msku.ceng.madlab.week10;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements NoteFragment.OnNoteListInteractionListener {
    boolean displayingEditor = false;
    Note editingNote;
    ArrayList<Note> notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        notes = retrieveNotes();

        if (!displayingEditor) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.container, NoteFragment.newInstance(notes));
            ft.commit();
        } else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, EditNoteFragment.newInstance(readContent(editingNote)));
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    private String readContent(Note editingNote) {
        Log.d("Readin Note with path",editingNote.getFilePath()); StringBuffer content = new StringBuffer(); try (BufferedReader reader = new BufferedReader(new FileReader(new File(editingNote.getFilePath())))) { String line; while ((line = reader.readLine()) != null){ content.append(line).append("\n"); } } catch (
                FileNotFoundException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); } return content.toString();
    }

    public void saveContent(Note editingNote, String content){
        editingNote.setDate(new Date()); String header = content.length() < 30 ? content : content.substring(0,30);
        editingNote.setHeader(header.replaceAll("\n", " ")); FileWriter writer = null; File file = new File(editingNote.getFilePath()); try { writer = new FileWriter(file); writer.write(content); } catch (
                IOException e) { e.printStackTrace(); }finally { if (writer != null) { try { writer.close(); } catch (IOException e) { e.printStackTrace(); } } } SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit(); Log.d("Saving tp Pref","key = " + file.getName() +" value = " + editingNote.getHeader()); editor.putString(file.getName(),editingNote.getHeader()); editor.commit();
    }

    private ArrayList<Note> retrieveNotes() {
        ArrayList<Note> notes = new ArrayList<>();
        File dir = getFilesDir();
        File[] files = dir.listFiles();
        for (File file : files) {
            Log.d("Retrieving", "absolute path = " + file.getAbsolutePath());
            Log.d("Retrieving", "name = " + file.getName());
            Note note = new Note();
            note.setFilePath(file.getAbsolutePath());
            note.setDate(new Date(file.lastModified()));
            String header = getPreferences(Context.MODE_PRIVATE).getString(file.getName(), "No Header!");
            note.setHeader(header);
            notes.add(note);
        }
        return notes;
    }

    @Override
    public void onNoteSelected(Note note) {
        editingNote = note;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, EditNoteFragment.newInstance(readContent(editingNote)), "edit_note");
        ft.addToBackStack("edit_note");
        ft.commit();
        displayingEditor = !displayingEditor;
        invalidateOptionsMenu();
    }

    @SuppressLint("LongLogTag")
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d("OnPrepareOptionsMenu new visible", menu.findItem(R.id.action_new).isVisible() + "");
        menu.findItem(R.id.action_new).setVisible(!displayingEditor);
        menu.findItem(R.id.action_close).setVisible(displayingEditor);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        displayingEditor = !displayingEditor;
        invalidateOptionsMenu();
        int itemId = item.getItemId();
        if (itemId == R.id.action_new) {
            editingNote = createNote();
            notes.add(editingNote);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, EditNoteFragment.newInstance(""), "edit_note");
            ft.addToBackStack(null);
            ft.commit();
            return true;
        } else if (itemId == R.id.action_close) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private Note createNote() {
        Note note = new Note(); SharedPreferences pref = getPreferences(Context.MODE_PRIVATE); int next = pref.getInt("next",1); File dir = getFilesDir(); String filePath = dir.getAbsolutePath()+"/note_"+next; Log.d("Create Note with path",filePath); note.setFilePath(filePath); SharedPreferences.Editor editor = pref.edit(); editor.putInt("next", next+1); editor.commit(); return note;
    }

    @Override
    public void onBackPressed() {
        EditNoteFragment editFragment = (EditNoteFragment) getSupportFragmentManager().findFragmentByTag("edit_note"); if (editFragment != null){ String content = editFragment.getContent(); saveContent(editingNote, content); } super.onBackPressed();
    }


}