package com.example.capstoneic;

import android.widget.Toast;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import edu.cmu.pocketsphinx.Hypothesis;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ ReportFragment.class })
public class MainActivityTest extends TestCase {
    @Mock
    private MainActivity mainActivity;

    @Test
    public void testOnCreate() {
        // Mockito.doNothing().when(SuperClass)

    }

    @Test
    public void testOnEndOfSpeech(){
        mainActivity.onEndOfSpeech();
    }

    @Test
    public void testOnTimeout(){

    }

    @Test
    public void testOnResult() {

    }
}
    /*
public class AddEditBeaconActivityTests {

    @Test
    public void test_onCreate() {
        // Mock some data
        mockStatic(ReportFragment.class);
        MyActivity activity = spy(new MyActivity());
        doNothing().when(activity).initScreen();
        doNothing().when(activity).setContentView(R.layout.layout);
        doReturn(mock(AppCompatDelegate.class)).when(activity).getDelegate();

        // Call the method
        activity.onCreate(null);

        // Verify that it worked
        verify(activity, times(1)).setContentView(R.layout.layout);
        verify(activity, times(1)).initScreen();
    }
}
*/