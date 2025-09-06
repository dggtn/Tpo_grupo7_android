package com.example.navigationfragments;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.example.tpo_mobile.services.AppService;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpFragment extends Fragment {

    @Inject
    AppService appService;

    private ListView listView;
    private List<String> pokemonDisplayList;
    private ArrayAdapter<String> adapter;

}
