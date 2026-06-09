package com.storemanagement.service.finance;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.finance.IncomeDAO;
import com.storemanagement.model.Income;

public class IncomeService extends GenericService<Income> {
    public IncomeService() {
        super(new IncomeDAO());
    }
}
