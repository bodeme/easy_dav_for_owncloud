/*******************************************************************************
 * Copyright (c) 2014 Ricki Hirner (bitfire web engineering).
 * Copyright (c) 2014 Markus Bode
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.bodeme.easycloud.syncadapter;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import at.bitfire.davdroid.Constants;
import at.bitfire.davdroid.URIUtils;

import com.bodeme.easycloud.R;

public class EnterCredentialsFragment extends Fragment implements TextWatcher {
	TextView textHttpWarning, textUrl;
	EditText editUserName, editPassword, editURL;
	Button btnNext;
	int typePosition;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.enter_credentials, container, false);

		textUrl = (TextView) v.findViewById(R.id.text_url);
				
		// protocol selection spinner
		textHttpWarning = (TextView) v.findViewById(R.id.http_warning);

		editURL = (EditText) v.findViewById(R.id.url);
		editURL.addTextChangedListener(this);
		
		editUserName = (EditText) v.findViewById(R.id.userName);
		editUserName.addTextChangedListener(this);
		
		editPassword = (EditText) v.findViewById(R.id.password);
		editPassword.addTextChangedListener(this);
		
		// Remove views for editing ownCloud-url, when constant is given
		if(Constants.OWNCLOUD_URL != null) {
			textUrl.setVisibility(View.GONE);
			editURL.setVisibility(View.GONE);
			textHttpWarning.setVisibility(View.GONE);
		}
		
		// ownCloud Type
		Spinner spnrType = (Spinner) v.findViewById(R.id.select_type);
		spnrType.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				typePosition = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				typePosition = 0;
			}
		});
		spnrType.setSelection(0);
		
		// hook into action bar
		setHasOptionsMenu(true);

		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.enter_credentials, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.next:
			queryServer();
			break;
		default:
			return false;
		}
		return true;
	}

	void queryServer() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		
		Bundle args = new Bundle();
		
		String url;
		String username = editUserName.getText().toString();
		if(Constants.OWNCLOUD_URL == null) {
			url = editURL.getText().toString();
		} else {
			url = Constants.OWNCLOUD_URL;
		}
		
		if(url.charAt(url.length() - 1) != '/') {
			url = url + "/";
		}
		
		if(0 == typePosition) {
			url += "remote.php/caldav/principals/"+username+"/";
		} else {
			url += "remote.php/carddav/addressbooks/"+username+"/default";
		}
		
		args.putString(QueryServerDialogFragment.EXTRA_BASE_URL, URIUtils.sanitize(url));
		args.putString(QueryServerDialogFragment.EXTRA_USER_NAME, username);
		args.putString(QueryServerDialogFragment.EXTRA_PASSWORD, editPassword.getText().toString());
		args.putBoolean(QueryServerDialogFragment.EXTRA_AUTH_PREEMPTIVE, true);
		
		DialogFragment dialog = new QueryServerDialogFragment();
		dialog.setArguments(args);
	    dialog.show(ft, QueryServerDialogFragment.class.getName());
	}

	
	// input validation
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		boolean ok =
			editUserName.getText().length() > 0 &&
			editPassword.getText().length() > 0;

		// check host name
		if(Constants.OWNCLOUD_URL == null) {
			try {
				URI uri = new URI(URIUtils.sanitize(editURL.getText().toString()));
				if (StringUtils.isBlank(uri.getHost()))
					ok = false;
			} catch (URISyntaxException e) {
				ok = false;
			}
		}
		
		MenuItem item = menu.findItem(R.id.next);
		item.setEnabled(ok);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		getActivity().invalidateOptionsMenu();

		if(Constants.OWNCLOUD_URL == null) {
			String url = editURL.getText().toString();
			boolean isHttps = (url.length() >= 8 && url.substring(0,  8).equals("https://"));
			
			textHttpWarning.setVisibility(isHttps ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
	}
}
