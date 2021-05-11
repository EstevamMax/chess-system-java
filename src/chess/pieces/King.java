package chess.pieces;

import boardgame.Board;
import boardgame.Position;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.Color;

public class King extends ChessPiece{
	
	private ChessMatch chessMatch;

	public King(Board board, Color color, ChessMatch chessMatch) {
		super(board, color);
		this.chessMatch = chessMatch;
	}

	@Override
	public String toString() {
		return "K";
	}
	
	private boolean canMove(Position position) { //Método que verifica se o movimento é permitido
		ChessPiece p = (ChessPiece)getBoard().piece(position); //Pega a peça da posição indicada
		return p == null || p.getColor() != getColor(); //Verifica se a peça não é nula e nem da mesma cor
	}
	
	private boolean testRookCastling(Position position) { //Testa se o Roque é permitido
		ChessPiece p = (ChessPiece)getBoard().piece(position); //Pega a peça na posição indicada
		return p != null && p instanceof Rook && p.getColor() == getColor() && p.getMoveCount() == 0;
	}

	@Override
	public boolean[][] possibleMoves() {
		boolean[][] mat = new boolean[getBoard().getRows()][getBoard().getColumns()];
		
		Position p = new Position(0, 0);
		
		//Above
		p.setValues(position.getRow() - 1, position.getColumn()); //Move uma casa para cima na matriz
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true; //permite o movimento caso a posição exista e a peça possa mover para ela
		}
		
		//Below
		p.setValues(position.getRow() + 1, position.getColumn());
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}
		
		//Left
		p.setValues(position.getRow(), position.getColumn() - 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}		
				
		//Right
		p.setValues(position.getRow(), position.getColumn() + 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}
		
		//NW
		p.setValues(position.getRow() - 1, position.getColumn() - 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}
		
		//NE
		p.setValues(position.getRow() - 1, position.getColumn() + 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}
		
		//SW
		p.setValues(position.getRow() + 1, position.getColumn() - 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}
		
		//SE
		p.setValues(position.getRow() + 1, position.getColumn() + 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}
		
		//#Specialmove castling kingside rook
		if(getMoveCount() == 0 && !chessMatch.getCheck()) { //Caso o rei não tenha realizado nenhum movimento e não esteja em check fazer:
			//Specialmove castling kingside rook
			Position posT1 = new Position(position.getRow(), position.getColumn() + 3); //Posição onde deve estar a torre do rei
				if(testRookCastling(posT1)) {//Caso a torre passe no teste:
					Position p1 = new Position(position.getRow(), position.getColumn() + 1);
					Position p2 = new Position(position.getRow(), position.getColumn() + 2);
					if(getBoard().piece(p1) == null && getBoard().piece(p2) == null) { //Caso as duas casas ao lado direito do rei estejam vazias:
						 mat[position.getRow()][position.getColumn() + 2] = true; //Permite o Roque
					}
				}
		//#Specialmove castling queenside rook
		Position posT2 = new Position(position.getRow(), position.getColumn() - 4); //Posição onde deve estar a torre da rainha
		if(testRookCastling(posT2)) {
			Position p1 = new Position(position.getRow(), position.getColumn() - 1);
			Position p2 = new Position(position.getRow(), position.getColumn() - 2);
			Position p3 = new Position(position.getRow(), position.getColumn() - 3);
			if(getBoard().piece(p1) == null && getBoard().piece(p2) == null && getBoard().piece(p3) == null) {
				 mat[position.getRow()][position.getColumn() - 2] = true;
					}
				}
			}

		return mat;
	}
}
