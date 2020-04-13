package com.xwr.smarttermin.base;

import com.xwr.smarttermin.main.FaceCollectFrag;
import com.xwr.smarttermin.main.IccFrag;
import com.xwr.smarttermin.main.IdCardFrag;
import com.xwr.smarttermin.main.IndexFrag;
import com.xwr.smarttermin.main.NotClaimedFrag;
import com.xwr.smarttermin.main.OwnExpenseFrag;
import com.xwr.smarttermin.main.PasswordFrag;
import com.xwr.smarttermin.main.ScanHealthCertificateFrag;
import com.xwr.smarttermin.main.ScanPayBarcodeFrag;
import com.xwr.smarttermin.main.SettleInfoFrag;
import com.xwr.smarttermin.main.SettleResultFrag;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Create by xwr on 2020/4/2
 * Describe:
 */
public class FragmentFactory {
  private static ConcurrentHashMap<Integer, BaseFragment> fragMap = new ConcurrentHashMap<>();

  public static BaseFragment getNewFrag(int position) {
    BaseFragment fragment = fragMap.get(position);
    if (fragment == null) {
      switch (position) {
        case 0://首页
          fragment = new IndexFrag();
          break;
        case 1://结算信息
          fragment = new SettleInfoFrag();
          break;
        case 2://扫描医保凭证
          fragment = new ScanHealthCertificateFrag();
          break;
        case 3://扫描付款码
          fragment = new ScanPayBarcodeFrag();
          break;
        case 4://人脸采集
          fragment = new FaceCollectFrag();
          break;
        case 5://未申领
          fragment = new NotClaimedFrag();
          break;
        case 6://输密
          fragment = new PasswordFrag();
          break;
        case 7:
          fragment = new IdCardFrag();
          break;
        case 8:
          fragment = new IccFrag();
          break;
        case 9://结算结果
          fragment = new SettleResultFrag();
          break;
        case 10://自费
          fragment = new OwnExpenseFrag();
          break;
      }
      assert fragment != null;
      fragMap.put(position, fragment);
    }
    return fragment;
  }
}
