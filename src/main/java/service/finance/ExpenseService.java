package service.finance;

import service.common.GenericService;

import dao.finance.ExpenseDAO;
import model.Expense;

public class ExpenseService extends GenericService<Expense> {
    public ExpenseService() {
        super(new ExpenseDAO());
    }
}
