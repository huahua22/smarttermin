package com.xwr.smarttermin.comm;

import com.xwr.smarttermin.inter.ChangeFragment;

/**
 * Create by xwr on 2020/4/2
 * Describe:
 */
public class FragmentParms {
  public static ChangeFragment sChangeFragment;


  public static void setFragmentSelected(ChangeFragment changeFragment) {
    sChangeFragment = changeFragment;
  }
}
