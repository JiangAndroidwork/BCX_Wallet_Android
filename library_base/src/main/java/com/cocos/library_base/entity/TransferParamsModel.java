package com.cocos.library_base.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ningkang.guo
 * @Date 2019/2/25
 */

@Getter
@Setter
public class TransferParamsModel extends BaseResult {

    /**
     * 转出账号
     */
    private String accountName;

    /**
     * 转入账号
     */
    private String receivablesAccountName;

    /**
     * 账户余额
     */
    private String accountBalance;

    /**
     * 转账金额
     */
    private String transferAmount;

    /**
     * 转账备注
     */
    private String transferMemo;

    private String password;

    private String transferSymbol;

}
