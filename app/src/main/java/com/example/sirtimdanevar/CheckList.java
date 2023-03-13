package com.example.sirtimdanevar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.sirtimdanevar.Adapter.CheckListAdapter;
import com.example.sirtimdanevar.Constants.MyConstants;
import com.example.sirtimdanevar.Data.AppData;
import com.example.sirtimdanevar.Database.RoomDB;
import com.example.sirtimdanevar.Models.Items;

import java.util.ArrayList;
import java.util.List;

public class CheckList extends AppCompatActivity {

    RecyclerView recyclerView;
    CheckListAdapter checkListAdapter;
    RoomDB database;
    List<Items> itemsList = new ArrayList<>(  );
    String header, show;

    EditText txtAdd;
    Button btnAdd;
    LinearLayout linearLayout;

    @Override
    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_one, menu);

        if (MyConstants.MY_SELECTIONS.equals(header)){
            menu.getItem(0).setVisible(false);
            menu.getItem(2).setVisible(false);
            menu.getItem(3).setVisible(false);
        }else if (MyConstants.MY_LIST_CAMEL_CASE.equals(header)){
            menu.getItem(1).setVisible(false);
        }

        MenuItem menuItem = menu.findItem(R.id.btnSearch );
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener( ) {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Items> mFinalList = new ArrayList<>(  );
                for (Items items : itemsList){
                    if (items.getItemname().toLowerCase().startsWith(newText.toLowerCase())){
                        mFinalList.add(items);
                    }
                }
                updateRecycler(mFinalList);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent( this,CheckList.class );
        AppData appData = new AppData(database,this);

        switch (item.getItemId()){
            case R.id.btnMySelections:
                intent.putExtra(MyConstants.HEADER_SMALL,MyConstants.MY_SELECTIONS);
                intent.putExtra(MyConstants.SHOW_SMALL,MyConstants.FALSE_STRING);
                startActivityForResult(intent,101);
                return true;

            case R.id.btnCustomList:
                intent.putExtra(MyConstants.HEADER_SMALL,MyConstants.MY_LIST_CAMEL_CASE);
                intent.putExtra(MyConstants.SHOW_SMALL,MyConstants.TRUE_STRING);
                startActivity(intent);
                return true;//calismazsa bunu sil

            case R.id.btnDeleteDefault:
                new AlertDialog.Builder(this)
                        .setTitle("Delete Default Data")
                        .setMessage("Are you sure?\n\nAs this will delete the data provided by(Pack your back) while installing")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener( ) {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                appData.persistDataByCategory(header,true);
                                itemsList = database.mainDao().getAll(header);
                                updateRecycler(itemsList);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener( ) {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).setIcon(R.drawable.ic_alert)
                        .show();
                return true;

            case R.id.btnReset:
                new AlertDialog.Builder(this)
                        .setTitle("Reset to Default")
                        .setMessage("Are you sure?\n\nAs this will load the default data provided by (Pack your bag) " +
                                "and will delete the custom data you have added in")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener( ) {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                appData.persistDataByCategory(header,false);
                                itemsList = database.mainDao().getAll(header);
                                updateRecycler(itemsList);
                            }
                        }).setNegativeButton("Confirm", new DialogInterface.OnClickListener( ) {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).setIcon(R.drawable.ic_alert)
                        .show();
                return true;

            case R.id.btnAboutUs:
                intent = new Intent( this,AboutUs.class );
                startActivity(intent);
                return true;

            case R.id.btnExit:
                this.finishAffinity();
                Toast.makeText(this, "Pack your Bag \n Exit completed", Toast.LENGTH_SHORT).show( );
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==101){
            itemsList=database.mainDao().getAll(header);
            updateRecycler(itemsList);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent= getIntent();
        header=intent.getStringExtra(MyConstants.HEADER_SMALL);
        show=intent.getStringExtra(MyConstants.SHOW_SMALL);

        getSupportActionBar().setTitle(header);

        txtAdd=findViewById(R.id.txtAdd);
        btnAdd=findViewById(R.id.btnAdd);
        recyclerView=findViewById(R.id.recyclerView);
        linearLayout=findViewById(R.id.linearLayout);

        database=RoomDB.getInstance(this);

        if (MyConstants.FALSE_STRING.equals(show)){
            linearLayout.setVisibility(View.GONE);
            itemsList = database.mainDao().getAllSelected(true);
        }else {
            itemsList = database.mainDao().getAll(header);
        }

        updateRecycler(itemsList);

        btnAdd.setOnClickListener(new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {
                String itemName = txtAdd.getText().toString();
                if (itemName!=null&&!itemName.isEmpty()){
                    addNewItem(itemName);
                    Toast.makeText(CheckList.this, "Item Added", Toast.LENGTH_SHORT).show( );
                }else {
                    Toast.makeText(CheckList.this, "Empty Cant be Added", Toast.LENGTH_SHORT).show( );
                }
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void addNewItem(String itemName){
        Items item = new Items();
        item.setChecked(false);
        item.setCategory(header);
        item.setItemname(itemName);
        item.setAddedby(MyConstants.USER_SMALL);
        database.mainDao().saveItem(item);
        itemsList = database.mainDao().getAll(header);
        updateRecycler(itemsList);
        recyclerView.scrollToPosition(checkListAdapter.getItemCount()-1);
        txtAdd.setText("");
    }

    private void updateRecycler(List<Items> itemsList){
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL));
        checkListAdapter = new CheckListAdapter( CheckList.this,itemsList,database,show );
        recyclerView.setAdapter(checkListAdapter);
    }
}