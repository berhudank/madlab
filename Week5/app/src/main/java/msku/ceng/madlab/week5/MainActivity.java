package msku.ceng.madlab.week5;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    static final String PLAYER_1 = "X";
    static final String PLAYER_2 = "O";

    boolean player1Turn = true;

    byte[][] board = new byte[3][3];

    int occupiedCells = 0;

    TableLayout table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.board), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        table = findViewById(R.id.board);
        for(int i = 0; i < 3; i++){
            TableRow row = (TableRow) table.getChildAt(i);
            for (int j = 0; j < 3; j++){
                Button button = (Button) row.getChildAt(j);
                button.setOnClickListener(new CellListener(i,j));
            }
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("player1Turn", player1Turn);
        outState.putInt("occupiedCells", occupiedCells);
        byte[] boardSingle = new byte[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                boardSingle[3*i+j] = board[i][j];
            }
        }
        outState.putByteArray("board", boardSingle);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        player1Turn = savedInstanceState.getBoolean("player1Turn");
        occupiedCells = savedInstanceState.getInt("occupiedCells");
        byte[] boardSingle = savedInstanceState.getByteArray("board");
        for (int i = 0; i < 9; i++) {
            board[i/3][i%3] = boardSingle[i];
        }
        TableLayout table = findViewById(R.id.board);
        for (int i = 0; i < 3; i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            for (int j = 0; j < 3; j++) {
                Button button = (Button) row.getChildAt(j);
                if(board[i][j] == 1){
                    button.setText("X");
                } else if (board[i][j] == 2) {
                    button.setText("0");
                }
            }
        }
    }

    class CellListener implements View.OnClickListener{
        int row, col;
        public CellListener(int row, int col){
            this.col = col;
            this.row = row;
        }
        @Override
        public void onClick(View v) {
            if(!isValidMove(row,col)){
                Toast.makeText(MainActivity.this, "cell is already occupied", Toast.LENGTH_LONG).show();
                return;
            }
            if(player1Turn){
                ((Button) v).setText(PLAYER_1);
                board[row][col] = 1;
            }
            else{
                ((Button) v).setText(PLAYER_2);
                board[row][col] = 2;
            }
            occupiedCells++;
            int gameEnded = gameEnded(row,col);
            if(gameEnded == -1) {
                player1Turn = !player1Turn;
            }
            else if (gameEnded == 0) {
                Toast.makeText(MainActivity.this, "it is a Draw", Toast.LENGTH_LONG).show();
            }
            else if (gameEnded(row,col) == 1) {
                Toast.makeText(MainActivity.this, "Player 1 Wins", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(MainActivity.this, "Player 2 Wins", Toast.LENGTH_LONG).show();
            }
        }

        public boolean isValidMove(int row, int col){
            return board[row][col] == 0;
        }

        public int gameEnded(int row, int col){

            int symbol = board[row][col];
            boolean win = true;
            for (int i=0; i<3; i++){
                if(board[i][col] != symbol){
                    win = false;
                    break;
                }
            }
            if(win){
                return symbol;
            }
            // check rows
            win = true;
            for (int i = 0; i < 3; i++) {
                if(board[row][i] != symbol){
                    win = false;
                    break;
                }
            }
            if (win){
                return symbol;
            }
            // check diagonals

            win = true;
            if(row - col == 2 || row - col == -2){
                for (int i = 0; i < 3; i++) {
                    if(board[2 - i][i] != symbol){
                        win = false;
                        break;
                    }
                }
            }
            else if (row - col == 0) {
                for (int i = 0; i < 3; i++) {
                    if(board[i][i] != symbol){
                        win = false;
                        break;
                    }
                }
                if(!win && row == 1){ // if win is false and cell is [1,1], check the other diagonal.
                    win = true;
                    for (int i = 0; i < 3; i++) {
                        if(board[2 - i][i] != symbol){
                            win = false;
                            break;
                        }
                    }
                }
            }
            else {
                win = false;
            }

            if(win){
                return symbol;
            }

            // if all cells are occupied, it is a draw.
            if(occupiedCells == 9){
                return 0;
            }

            return -1;
        }

    }
}