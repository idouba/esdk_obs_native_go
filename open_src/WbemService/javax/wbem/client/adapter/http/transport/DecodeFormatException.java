/*
 * 项 目 名:  ISM V100R006C00
 * 版    权:  Huawei Technologies Co., Ltd. Copyright 2010,  All rights reserved.
 * 描    述:  Huawei PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.wbem.client.adapter.http.transport;

import java.io.IOException;
/**
 * 提供一个用于BASE64的辅助类。
 *
 * @author  h90005710
 * @version  ISM V100R006C00,2014-01-24
 * @since  V100R006C00
 */
public class DecodeFormatException extends IOException {
        public DecodeFormatException(String s) {
                super(s);
        }
}
