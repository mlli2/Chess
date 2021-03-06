package game;
import board.*;
import pieces.*;
import player.*;
import gui.Controller;



import java.util.ArrayList;
import java.awt.Point;

public class Game{
	protected boolean inCheck;
	protected boolean checkMate;
	protected int turn; // odd = black's turn, even = white's turn
	protected Board board;
	protected boolean forcedStalemate;
	protected boolean forfeit;
	protected int blackScore;
	protected int whiteScore;
	
	public static void main(String[] args) {    	
	    Controller controller = new Controller();
	}
	
	/**
	 * Constructor for a game object
	 * Initializes the check status and checkmate status to false
	 * Initializes the turn to 0, so white moves first
	 * Creates the board and sets up players/pieces
	 */
	public Game()
	{
		turn = 0;
		board = new Board();
		board.setupAll();
		forcedStalemate = false;
		forfeit = false;
		blackScore = 0;
		whiteScore = 0;
	}
	
	/**
	 * Runs the game loop that constantly checks for checkmate
	 */
	public class startLoop implements Runnable
	{	public void run(){
			System.out.println("Game start");
			while(!isStalemate(board) && !forcedStalemate)
			{
				if (getCurrentPlayer().getTeam().equals("black"))
				{
					if (isCheckmate(board,board.getBlackPlayer()) || forfeit)
					{
						whiteScore++;
						break;
					}
				}	
				else
				{
					if (isCheckmate(board,board.getWhitePlayer()) || forfeit)
					{
						blackScore++;
						break;
					}
				}
			}
			System.out.println("Game over!");
			System.out.println("White score: " + whiteScore);
			forfeit = false;
			System.out.println("Black score: " + blackScore);
			
		}
	}
	
	/**
	 * Resets the current board and players
	 */
	public void resetGame()
	{
		forceStalemate();
		board = new Board();
		board.setupAll();
		forcedStalemate = false;
		Thread t = new Thread(new startLoop());
		t.start();
	}
	/**
	 * Changes the forceCheckmate boolean to true to end the gameloop
	 */
	public void forceStalemate()
	{
		forcedStalemate = true;
	}
	
	/**
	 * Sets a new board for a game
	 * @param newBoard, the new board we are playing on
	 */
	public void setBoard(Board newBoard)
	{
		board = newBoard;
	}
	
	
	/**
	 * Gets the board of the current game
	 * @return the board object that the game is being played on
	 */
	public Board getBoard()
	{
		return board;
	}
	
	/**
	 * Increments the turn counter
	 * Effectively rotates the player between black and white
	 */
	public void incrementTurn()
	{
		turn++;
	}
	
	/**
	 * Decrements the turn counter for an undo move
	 */
	public void decrementTurn()
	{
		turn--;
	}
	
	/**
	 * Gets the player whose turn it currently is
	 * @return the player object who currently controls the board
	 */
	public Player getCurrentPlayer()
	{
		if (turn%2 == 0)
			return board.getWhitePlayer();
		return board.getBlackPlayer();
	}
	
	/**
	 * getter for the black score
	 * @return the score of the black player
	 */
	public int getBlackScore()
	{
		return blackScore;
	}

	/**
	 * getter for the white score
	 * @return the score of the white player
	 */
	public int getWhiteScore()
	{
		return whiteScore;
	}
	
	/**
	 * Forces the game loop to exit
	 */
	public void forfeitGame()
	{
		forfeit = true;
	}
	
	/**
	 * Checks if the game is in stalemate by detecting that current player's units have no valid moves
	 * @param board, the board we are playing on
	 * @return true if the game is in stalemate, false otherwise
	 */
	public boolean isStalemate(Board board)
	{
		Player blackPlayer = board.getBlackPlayer();
		Player whitePlayer = board.getWhitePlayer();
		ArrayList<Piece> blackPieceList = blackPlayer.getPieceList();
		ArrayList<Piece> whitePieceList = whitePlayer.getPieceList();
		for(int index = 0; index < blackPieceList.size(); index++)
		{
			Piece piece = blackPieceList.get(index);
			if (piece.getMoveList().size() != 0)
				return false;
		}
		
		for(int index = 0; index < whitePieceList.size(); index++)
		{
			Piece piece = whitePieceList.get(index);
			if (piece.getMoveList().size() != 0)
				return false;
		}
		return true;
	}
	
	/**
	 * Checks if a particular king is in check by comparing it to the opposing team's movelist
	 * @param king, the king we are inspecting
	 * @param board, the board we are playing on
	 * @return true if the king is in check, false otherwise
	 */
	public boolean isInCheck(Board board, Player player)
	{
		Player whitePlayer = board.getWhitePlayer();
		Player blackPlayer = board.getBlackPlayer();
		King king = player.getKing();
		
		if (player.getTeam().equals("black"))
		{
			for(int index = 0; index < whitePlayer.getSizeOfPieceList(); index++)
			{
				Piece piece = whitePlayer.getPieceList().get(index);
				ArrayList<Point> moveList = piece.getMoveList();
				if(moveList.size()==0)
					continue;
				for (int move = 0; move<moveList.size(); move++)
				{
					if (king.getLocation().equals(moveList.get(move)))
						return true;
				}
			}
		}
		
		else //king is white
		{
			for(int index = 0; index < blackPlayer.getSizeOfPieceList(); index++)
			{
				Piece piece = blackPlayer.getPieceList().get(index);
				ArrayList<Point> moveList = piece.getMoveList();
				if(moveList.size()==0)
					continue;
				for (int move = 0; move<moveList.size(); move++)
				{
					if (king.getLocation().equals(moveList.get(move)))
						return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks if the game is in a checkmate state by detecting that a king has no moves and cannot be saved
	 * Runs every single possible move to see if it can cancel the check
	 * @param board, the board we are playing on
	 * @param player, the player that we are determining if is in checkmate
	 * @return true if the player is in checkmate, false otherwise
	 */
	
	public boolean isCheckmate(Board board, Player player)
	{
		Piece pieceTaken = null;
		int originalX;
		int originalY;
		
		if (isInCheck(board, player))
		{
			King king = player.getKing();
			if (king.getMoveList().size() == 0)
				return true;
			for(int index = 0; index < player.getPieceList().size(); index++)
			{
				Piece piece = player.getPieceList().get(index);
				ArrayList<Point> moveList = piece.getMoveList();
				originalX = piece.getXLocation();
				originalY = piece.getYLocation();
				for (int move = 0; move<moveList.size(); move++)
				{
					Point testMove = moveList.get(move);
					int testX = (int)testMove.getX();
					int testY = (int)testMove.getY();
					pieceTaken = board.getPiece(testX, testY);
					board.move(piece, testX, testY);
					if (!isInCheck(board,player))
					{
						board.move(piece, originalX, originalY);
						if (pieceTaken!=null)
							board.addPiece(pieceTaken, testX, testY);
						return false;
					}
					board.move(piece, originalX, originalY);
					if (pieceTaken!=null)
						board.addPiece(pieceTaken, testX, testY);
				}
			} 
			return true;
		}
		return false;
	}	
	
	/**
	 * A valid move is defined as being in the piece's moveList and not causing a check on the King
	 * Checks if a designated move by a piece is a valid move for the piece
	 * @param piece, the piece that is trying to be moved
	 * @param newX, the new desired x-coordinate
	 * @param newY, the new desired y-coordinate
	 * @return true if the move is valid, false otherwise
	 */
	public boolean isValidMove(Piece piece, int newX, int newY)
	{
		Player player;
		String team = piece.getTeam();
		if (team.equals("black"))
			player = getBoard().getBlackPlayer();
		else
			player = getBoard().getWhitePlayer();
		Point point = new Point(newX, newY);
		if(!player.getTeam().equals(piece.getTeam()) || !piece.isInMoveList(point) || putsKingInCheck(piece, point))
			return false;
		return true;
	}
	
	/**
	 * Checks if moving a certain piece to a certain point causes its king to be in check
	 * @param board, the board we are playing on
	 * @param piece, the piece that is being moved
	 * @param point, the new location the piece will be at
	 * @return true if the king is in check as a result of the move, false otherwise
	 */
	public boolean putsKingInCheck(Piece piece, Point point)
	{
		Player player;
		String kingColor = piece.getTeam();	
		if (kingColor.equals("black"))
			player = board.getBlackPlayer();
		else
			player = board.getWhitePlayer();
		int originalX = piece.getXLocation();
		int originalY = piece.getYLocation();
		int testX = (int)point.getX();
		int testY = (int)point.getY();
		Piece pieceTaken = board.getPiece(testX, testY);
		board.move(piece, testX, testY);
		if (isInCheck(board,player))
		{
			board.move(piece, originalX, originalY);
			if (pieceTaken!=null)
				board.addPiece(pieceTaken, testX, testY);
			return true;
		}
		board.move(piece, originalX, originalY);
		if (pieceTaken!=null)
			board.addPiece(pieceTaken, testX, testY);
		return false;
	}
}
