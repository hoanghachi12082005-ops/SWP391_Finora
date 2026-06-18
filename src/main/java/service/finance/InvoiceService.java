package service.finance;

import service.common.GenericService;

import dao.finance.InvoiceDAO;
import model.Invoice;

public class InvoiceService extends GenericService<Invoice> {
    public InvoiceService() {
        super(new InvoiceDAO());
    }
}
