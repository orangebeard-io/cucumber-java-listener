package io.orangebeard.listener;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DataTableHelperTest {

    @Test
    public void a_data_table_can_be_mapped_to_md_easily(){
        var result = DataTableHelper.toMdTable(List.of(
                List.of("header 1", "header 2", "header 3"),
                List.of("content 1", "content 2", "content 3")));

        assertThat(result).isEqualTo("| header 1 | header 2 | header 3 | \n| -- | -- | -- | \n| content 1 | content 2 | content 3 |");
    }

}