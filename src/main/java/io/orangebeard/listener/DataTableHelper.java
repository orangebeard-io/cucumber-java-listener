package io.orangebeard.listener;

import java.util.List;
import java.util.stream.Collectors;

public class DataTableHelper {

    public static String toMdTable(List<List<String>> dataTable) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < dataTable.size(); i++) {
            result.append(dataTable.get(i).stream().map(cell -> " | " + cell).collect(Collectors.joining()).trim()).append(" | \n");
            if (i == 0) {
                result.append(dataTable.get(i).stream().map(cell -> "| -- ").collect(Collectors.joining()).trim()).append(" | \n");
            }
        }
        return result.toString().trim();
    }
}
