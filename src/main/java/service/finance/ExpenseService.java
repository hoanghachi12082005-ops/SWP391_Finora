package com.storemanagement.service.finance;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.finance.ExpenseDAO;
import com.storemanagement.model.Expense;

public class ExpenseService extends GenericService<Expense> {
    public ExpenseService() {
        super(new ExpenseDAO());
    }
}
