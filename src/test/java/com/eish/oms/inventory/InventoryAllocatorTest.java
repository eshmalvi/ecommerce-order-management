package com.eish.oms.inventory;

import static com.eish.oms.SeedData.HEADPHONES_EAST;
import static com.eish.oms.SeedData.HEADPHONES_TOTAL;
import static com.eish.oms.SeedData.HEADPHONES_WEST;
import static com.eish.oms.SeedData.SKU_HEADPHONES;
import static com.eish.oms.SeedData.WAREHOUSE_EAST;
import static com.eish.oms.SeedData.WAREHOUSE_WEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.eish.oms.AbstractIntegrationTest;
import com.eish.oms.Parallel;
import com.eish.oms.common.InsufficientStockException;

/**
 * The allocator against the real database. Seed: headphones 6 in WH-EAST, 4 in WH-WEST.
 */
class InventoryAllocatorTest extends AbstractIntegrationTest {

    private static final int BUYERS = 50;

    @Autowired
    private InventoryAllocator allocator;

    @Test
    void takesFromTheFullestWarehouseFirst() {
        long chosen = allocator.allocate(productId(SKU_HEADPHONES), 1);

        assertThat(chosen).isEqualTo(warehouseId(WAREHOUSE_EAST));
        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_EAST)).isEqualTo(HEADPHONES_EAST - 1);
        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_WEST)).isEqualTo(HEADPHONES_WEST);
    }

    @Test
    void fallsThroughToTheNextWarehouseWhenTheFullestCannotCoverTheLine() {
        long headphones = productId(SKU_HEADPHONES);

        assertThat(allocator.allocate(headphones, 4)).isEqualTo(warehouseId(WAREHOUSE_EAST));   // east 6 -> 2
        assertThat(allocator.allocate(headphones, 4)).isEqualTo(warehouseId(WAREHOUSE_WEST));   // east has 2, west 4 -> 0

        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_EAST)).isEqualTo(2);
        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_WEST)).isZero();
    }

    @Test
    void refusesWhenNoSingleWarehouseHasEnough() {
        long headphones = productId(SKU_HEADPHONES);

        // 10 units exist in total, but a line ships from one warehouse and the fullest has 6.
        assertThatThrownBy(() -> allocator.allocate(headphones, 7))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("requested 7");

        assertThat(totalStock(SKU_HEADPHONES)).isEqualTo(HEADPHONES_TOTAL);
    }

    /**
     * The claim the whole design rests on. Fifty buyers race for ten units across two warehouses:
     * exactly ten win, forty are told there is no stock, and the shelves end at zero, never below.
     * Repeated because a race that passes once proves little.
     */
    @RepeatedTest(5)
    void fiftyConcurrentBuyersNeverOversellTenUnits() throws Exception {
        long headphones = productId(SKU_HEADPHONES);

        List<Boolean> gotOne = Parallel.run(BUYERS, buyer -> () -> {
            try {
                allocator.allocate(headphones, 1);
                return true;
            } catch (InsufficientStockException outOfStock) {
                return false;
            }
        });

        long winners = gotOne.stream().filter(Boolean::booleanValue).count();
        assertThat(winners).isEqualTo(HEADPHONES_TOTAL);
        assertThat(gotOne).hasSize(BUYERS);
        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_EAST)).isZero();
        assertThat(stock(SKU_HEADPHONES, WAREHOUSE_WEST)).isZero();
        assertThat(lowestStockAnywhere()).isGreaterThanOrEqualTo(0);
    }

    private int lowestStockAnywhere() {
        return jdbc.sql("select min(quantity) from inventory").query(Integer.class).single();
    }
}
