package com.sogou.nlu.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuhuahai on 2017/12/1.
 */
public class TagUtils {

    /**
     * 解析tag，得到map
     * @param tag  such as: stage=beta;version=1.0
     * @return
     */
    static public Map<String,String> parseTag(String tag){
        Map<String,String> result = new HashMap<String,String>();
        if(tag!=null && !tag.equals("")){
            String[] items = tag.split(";");
            for(String item : items){
                String[] arr = item.split("=");
                if(arr.length == 2){
                    result.put(arr[0],arr[1]);
                }
            }
        }
        return result;
    }


}
