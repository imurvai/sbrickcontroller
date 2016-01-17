package com.scn.sbrickcontroller;

import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Game controller action listener interface.
 */
public interface GameControllerActionListener {

    boolean onKeyDown(int keyCode, KeyEvent event);

    boolean onKeyUp(int keyCode, KeyEvent event);

    boolean onGenericMotionEvent(MotionEvent event);
}
