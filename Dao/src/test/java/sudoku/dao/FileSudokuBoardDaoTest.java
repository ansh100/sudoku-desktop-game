package sudoku.dao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import sudoku.dao.factories.SudokuBoardDaoFactory;
import sudoku.dao.interfaces.Dao;
import sudoku.model.exceptions.FillingBoardSudokuException;
import sudoku.model.models.SudokuBoard;
import sudoku.model.solver.BacktrackingSudokuSolver;

public class FileSudokuBoardDaoTest {
    // Directory path for testing
    private static final String TEST_DIRECTORY = "test_dir";

    private void cleanUpDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && !file.getName().startsWith(".git")) {
                    file.delete();
                }
            }
        }
    }

    @AfterEach
    void tearDown() {
        cleanUpDirectory(new File(TEST_DIRECTORY));
    }

    @Test
    public void testWriteAndRead() {
        SudokuBoard sampleBoard = new SudokuBoard(new BacktrackingSudokuSolver());
        try {
            sampleBoard.solveGame();
        } catch (FillingBoardSudokuException e) {
            e.printStackTrace();
        }

        sampleBoard.setField(0, 0, 5);
        sampleBoard.setField(0, 5, 9);
        sampleBoard.setField(4, 7, 2);

        final String boardName = "test_board";

        assertNotNull(sampleBoard);

        try (Dao<SudokuBoard> dao = SudokuBoardDaoFactory.createSudokuBoardDao(TEST_DIRECTORY)) {
            assertDoesNotThrow(() -> dao.write(boardName, sampleBoard));

            assertTrue(new File(TEST_DIRECTORY, boardName).exists());

            SudokuBoard readBoard = assertDoesNotThrow(() -> dao.read(boardName));
            assertNotNull(readBoard);

            assertEquals(sampleBoard, readBoard);

            assertEquals(sampleBoard.getField(0, 0), readBoard.getField(0, 0));
            assertEquals(sampleBoard.getField(0, 5), readBoard.getField(0, 5));
            assertEquals(sampleBoard.getField(4, 7), readBoard.getField(4, 7));

        } catch (Exception e) {
            Assertions.fail();
        }

    }
    //new tests
    @Test
    public void testReadThrowsSudokuReadException() {
        try (Dao<SudokuBoard> dao = SudokuBoardDaoFactory.createSudokuBoardDao(TEST_DIRECTORY)) {
            assertThrows(sudoku.dao.exceptions.SudokuReadException.class, () -> {
                dao.read("this_file_does_not_exist.txt");
            });
        } catch (Exception e) {
            Assertions.fail("The try-with-resources block failed.");
        }
    }

    @Test
    public void testWriteThrowsSudokuWriteException() {
        try (Dao<SudokuBoard> dao = SudokuBoardDaoFactory.createSudokuBoardDao("fake_dir/another_dir/locked_dir")) {
            SudokuBoard board = new SudokuBoard(new BacktrackingSudokuSolver());

            assertThrows(sudoku.dao.exceptions.SudokuWriteException.class, () -> {
                dao.write("test_board", board);
            });
        } catch (Exception e) {
            Assertions.fail("The try-with-resources block failed.");
        }
    }

    @Test
    public void testNamesWhenDirectoryDoesNotExist() {
        try (Dao<SudokuBoard> dao = SudokuBoardDaoFactory.createSudokuBoardDao("non_existent_folder_12345")) {
            assertTrue(dao.names().isEmpty(), "The names list should be empty for a non-existent directory.");
        } catch (Exception e) {
            Assertions.fail("The try-with-resources block failed.");
        }
    }

}
