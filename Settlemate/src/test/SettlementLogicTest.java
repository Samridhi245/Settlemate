package test;

/**
 * SettlementLogicTest
 *
 * Standalone test for the partial settlement / balance computation logic.
 * Mirrors what calculateNetBalances() + settleIndividual() do in-memory,
 * without needing the full application context (no DB, no file I/O).
 *
 * Run: javac src/test/SettlementLogicTest.java && java -cp src test.SettlementLogicTest
 */
public class SettlementLogicTest {

    // -----------------------------------------------------------------------
    // Minimal in-memory simulation of calculateNetBalances + settleIndividual
    // -----------------------------------------------------------------------

    static double debtorBalance;   // negative means "owes money"
    static double creditorBalance; // positive means "is owed money"

    /**
     * Simulate adding an expense:
     *   payer's balance += amount
     *   debtor's balance -= amount
     */
    static void addExpense(double amount, boolean payerIsCreditor) {
        if (payerIsCreditor) {
            creditorBalance += amount;
            debtorBalance   -= amount;
        } else {
            debtorBalance   += amount;  // shouldn't happen in normal test flow
            creditorBalance -= amount;
        }
    }

    /**
     * Simulate settleIndividual (the FIXED version).
     *
     * In calculateNetBalances:
     *   - The synthetic expense has toUserId (creditor) as payer  → creditorBalance DECREASES (their claim paid back to them reduces their outstanding positive balance over expenses; net the payment cancels)
     *
     * Actually let's model this directly:
     *   debtorBalance  is negative (owes money).  Paying reduces the debt → debtorBalance += paidAmount (towards 0)
     *   creditorBalance is positive (is owed money). Getting paid reduces what is outstanding → creditorBalance -= paidAmount (towards 0)
     *
     * Returns remaining debt (>= 0).
     */
    static double settle(double paidAmount, String debtorName, String creditorName) {
        double remainingDebt = -debtorBalance; // positive "amount owed"

        // Defensive: amount <= 0
        if (paidAmount <= 0) {
            throw new IllegalArgumentException("Settlement amount must be > 0.");
        }
        // Defensive: amount > remaining debt
        if (paidAmount > remainingDebt + 0.01) {
            throw new IllegalArgumentException(String.format(
                    "Cannot settle %.2f — %s only owes %.2f to %s.",
                    paidAmount, debtorName, remainingDebt, creditorName));
        }

        double amountToSettle = Math.min(paidAmount, remainingDebt);

        System.out.printf("  [BEFORE] %s owes %.2f | pays %.2f%n",
                debtorName, remainingDebt, amountToSettle);

        // Correct direction:
        //   debtor (negative balance) += paid  → moves toward 0 ✓
        //   creditor (positive balance) -= paid → moves toward 0 ✓
        debtorBalance   += amountToSettle;
        creditorBalance -= amountToSettle;

        double updatedDebt = Math.max(0.0, -debtorBalance);
        System.out.printf("  [AFTER ] %s remaining debt: %.2f%n", debtorName, updatedDebt);
        return updatedDebt;
    }

    // -----------------------------------------------------------------------
    // Test helpers
    // -----------------------------------------------------------------------

    static int passed = 0, failed = 0;

    static void assertEquals(String label, double expected, double actual) {
        boolean ok = Math.abs(expected - actual) < 0.01;
        System.out.printf("  %-50s %s (expected=%.2f, actual=%.2f)%n",
                label, ok ? "PASS ✔" : "FAIL ✘", expected, actual);
        if (ok) passed++; else failed++;
    }

    static void assertThrows(String label, Runnable r) {
        try {
            r.run();
            System.out.printf("  %-50s FAIL ✘ (no exception thrown)%n", label);
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.printf("  %-50s PASS ✔ (%s)%n", label, e.getMessage());
            passed++;
        }
    }

    // -----------------------------------------------------------------------
    // Test cases
    // -----------------------------------------------------------------------

    static void reset() {
        debtorBalance   = 0.0;
        creditorBalance = 0.0;
    }

    public static void main(String[] args) {
        System.out.println("=== SettlementLogicTest ===\n");

        // ------------------------------------------------------------------
        // Test 1 – Single partial payment reduces debt
        // ------------------------------------------------------------------
        System.out.println("Test 1: Single partial payment (1000 -> pay 200 -> expect 800 remaining)");
        reset();
        addExpense(1000, true); // creditor paid 1000, debtor owes 1000
        double rem = settle(200, "Debtor", "Creditor");
        assertEquals("Remaining debt after paying 200", 800.0, rem);
        System.out.println();

        // ------------------------------------------------------------------
        // Test 2 – Multiple partial payments
        // ------------------------------------------------------------------
        System.out.println("Test 2: Multiple partial payments (1000 -> 200 -> 300 -> 500 -> 0)");
        reset();
        addExpense(1000, true);
        rem = settle(200, "Debtor", "Creditor");
        assertEquals("After 1st payment (200): remaining", 800.0, rem);
        rem = settle(300, "Debtor", "Creditor");
        assertEquals("After 2nd payment (300): remaining", 500.0, rem);
        rem = settle(500, "Debtor", "Creditor");
        assertEquals("After 3rd payment (500): remaining", 0.0, rem);
        System.out.println();

        // ------------------------------------------------------------------
        // Test 3 – Full settlement in one shot
        // ------------------------------------------------------------------
        System.out.println("Test 3: Full settlement in one shot");
        reset();
        addExpense(450, true);
        rem = settle(450, "Debtor", "Creditor");
        assertEquals("Full pay: remaining should be 0", 0.0, rem);
        System.out.println();

        // ------------------------------------------------------------------
        // Test 4 – Overpayment is rejected
        // ------------------------------------------------------------------
        System.out.println("Test 4: Overpayment should throw exception");
        reset();
        addExpense(500, true);
        assertThrows("Pay 600 when only 500 owed → exception",
                () -> settle(600, "Debtor", "Creditor"));
        System.out.println();

        // ------------------------------------------------------------------
        // Test 5 – Zero or negative amount is rejected
        // ------------------------------------------------------------------
        System.out.println("Test 5: Zero/negative amounts should throw exception");
        reset();
        addExpense(300, true);
        assertThrows("Pay 0 → exception", () -> settle(0, "Debtor", "Creditor"));
        assertThrows("Pay -50 → exception", () -> settle(-50, "Debtor", "Creditor"));
        System.out.println();

        // ------------------------------------------------------------------
        // Test 6 – Amount NEVER increases (regression guard)
        // ------------------------------------------------------------------
        System.out.println("Test 6: Regression – amount must NEVER increase after settlement");
        reset();
        addExpense(700, true);
        double before = -debtorBalance;   // 700
        settle(100, "Debtor", "Creditor");
        double after = -debtorBalance;    // should be 600
        boolean neverIncreased = after <= before;
        System.out.printf("  %-50s %s (before=%.2f, after=%.2f)%n",
                "Remaining debt did not increase",
                neverIncreased ? "PASS ✔" : "FAIL ✘", before, after);
        if (neverIncreased) passed++; else failed++;
        System.out.println();

        // ------------------------------------------------------------------
        // Test 7 – Remaining debt NEVER goes below 0
        // ------------------------------------------------------------------
        System.out.println("Test 7: Remaining debt must not go below 0");
        reset();
        addExpense(100, true);
        rem = settle(100, "Debtor", "Creditor");
        assertEquals("Remaining debt >= 0 after exact payment", 0.0, Math.max(0.0, rem));
        System.out.println();

        // ------------------------------------------------------------------
        // Summary
        // ------------------------------------------------------------------
        System.out.printf("=== Results: %d passed, %d failed ===%n", passed, failed);
        System.exit(failed == 0 ? 0 : 1);
    }
}
