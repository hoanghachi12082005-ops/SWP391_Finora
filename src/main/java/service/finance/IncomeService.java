package service.finance;

import service.common.GenericService;

import dao.finance.IncomeDAO;
import model.Income;

public class IncomeService extends GenericService<Income> {
    public IncomeService() {
        super(new IncomeDAO());
    }
}
