package com.bootcamp.reportservice.dto;

import java.util.List;

/** Una cuenta del cliente con sus movimientos en el intervalo pedido. */
public record AccountReportEntry(AccountInfo account, List<AccountMovementInfo> movements) {
}
