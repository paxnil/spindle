/**********************************************************************
Copyright (c) 2002  Widespace, OU  and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://solareclipse.sourceforge.net/legal/cpl-v10.html

Contributors:
	Igor Malinin - initial contribution

$Id$
**********************************************************************/
package net.sf.solareclipse.xml.internal.ui.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import java.util.HashMap;
import java.util.Map;


/**
 * 
 * 
 * @author Igor Malinin
 */
public class XMLPartitionScanner implements IPartitionTokenScanner {
	public static final String XML_PI         = "__xml_processing_instruction";
	public static final String XML_COMMENT    = "__xml_comment";
	public static final String XML_DECL       = "__xml_declaration";
	public static final String XML_TAG        = "__xml_tag";
	public static final String XML_ATTRIBUTE  = "__xml_attribute";
	public static final String XML_CDATA      = "__xml_cdata";

	public static final String DTD_INTERNAL         = "__dtd_internal";
	public static final String DTD_INTERNAL_PI      = "__dtd_internal_pi";
	public static final String DTD_INTERNAL_COMMENT = "__dtd_internal_comment";
	public static final String DTD_INTERNAL_DECL    = "__dtd_internal_declaration";
	public static final String DTD_CONDITIONAL      = "__dtd_conditional";

	public static final int STATE_DEFAULT     = 0;
	public static final int STATE_TAG         = 1;
	public static final int STATE_DECL        = 2;
	public static final int STATE_CDATA       = 4;

	public static final int STATE_INTERNAL    = 8;

	private IDocument document;
	private int begin;
	private int end;

	private int offset;
	private int length;

	private int position;
	private int state;

	private boolean parsedtd;

	private Map tokens = new HashMap();

	public XMLPartitionScanner( boolean parsedtd ) {
		this.parsedtd = parsedtd;
	}

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#nextToken()
	 */
	public IToken nextToken() {
		offset += length;

		switch ( state ) {
			case STATE_TAG:
				return nextTagToken();

			case STATE_DECL:
				return nextDeclToken();

			case STATE_CDATA:
				return nextCDATAToken();
		}

		switch ( read() ) {
			case ICharacterScanner.EOF:
				state = STATE_DEFAULT;
				return getToken( null );

			case '<':
				switch ( read() ) {
					case ICharacterScanner.EOF:
						if ( parsedtd || isInternal() ) {
							break;
						}

						state = STATE_DEFAULT;
						return getToken( XML_TAG );

					case '?': // <?  <?PI
						return nextPIToken();

					case '!': // <!  <!DEFINITION or <![CDATA[ or <!--COMMENT
						switch ( read() ) {
							case ICharacterScanner.EOF:
								state = STATE_DEFAULT;
								return getToken( XML_TAG );

							case '-': // <!-  <!--COMMENT
								switch ( read() ) {
									case ICharacterScanner.EOF:
										return nextDeclToken();

									case '-': // <!--
										return nextCommentToken();
								}

							case '[': // <![  <![CDATA[ or <![%cond;[
								if ( parsedtd ) {
									return nextConditionalToken();
								}

								if ( !isInternal() ) {
									return nextCDATAToken();
								}
						}

						return nextDeclToken();
				}

				if ( parsedtd || isInternal() ) {
					break;
				}

				unread();

				return nextTagToken();

			case ']':
				if ( isInternal() ) {
					unread();

					state = STATE_DECL;
					length = 0;
					return nextToken();
				}
		}

		loop: while ( true ) {
			switch ( read() ) {
				case ICharacterScanner.EOF:
					state = STATE_DEFAULT;
					return getToken( null );

				case '<':
					if ( parsedtd || isInternal() ) {
						switch ( read() ) {
							case ICharacterScanner.EOF:
								state = STATE_DEFAULT;
								return getToken( null );

							case '!':
							case '?':
								unread();
								break;

							default:
								continue loop;
						}
					}

					unread();

					state &= STATE_INTERNAL;
					return getToken( isInternal() ? DTD_INTERNAL : null );

				case ']':
					if ( isInternal() ) {
						unread();

						state = STATE_DECL;
						if ( position == offset ) {
							// nothing between
							length = 0;
							return nextToken();
						}

						return getToken( DTD_INTERNAL );
					}
			}
		}
	}

	private IToken nextTagToken() {
		int quot = read();

		switch ( quot ) {
			case ICharacterScanner.EOF:
			case '>':
				state = STATE_DEFAULT;
				return getToken( XML_TAG );

			case '"': case '\'':
				while ( true ) {
					int ch = read();

					if ( ch == quot ) {
						state = STATE_TAG;
						return getToken( XML_ATTRIBUTE );
					}

					switch ( ch ) {
						case '<':
							unread();

						case ICharacterScanner.EOF:
							state = STATE_DEFAULT;
							return getToken( XML_ATTRIBUTE );
					}
				}
		}

		while ( true ) {
			switch ( read() ) {
				case '<':
					unread();

				case ICharacterScanner.EOF:
				case '>':
					state = STATE_DEFAULT;
					return getToken( XML_TAG );

				case '"': case '\'':
					unread();

					state = STATE_TAG;
					return getToken( XML_TAG );
			}
		}
	}

	private IToken nextDeclToken() {
		loop: while ( true ) {
			switch ( read() ) {
				case ICharacterScanner.EOF:
					state = STATE_DEFAULT;
					return getToken( isInternal()
						? DTD_INTERNAL_DECL : XML_DECL );

				case '<':
					if ( parsedtd || isInternal() ) {
						switch ( read() ) {
							case ICharacterScanner.EOF:
								state = STATE_DEFAULT;
								return getToken( isInternal()
									? DTD_INTERNAL : null );

							case '!':
							case '?':
								unread();
								break;

							default:
								continue loop;
						}
					}

					unread();

				case '>':
					state &= STATE_INTERNAL;
					return getToken( isInternal()
						? DTD_INTERNAL_DECL : XML_DECL );

				case '[': // <!DOCTYPE xxx [dtd]>
					if ( !isInternal() ) {
						state = STATE_INTERNAL;
						return getToken( XML_DECL );
					}
			}
		}
	}

	private IToken nextCommentToken() {
		state &= STATE_INTERNAL;

		loop: while ( true ) {
			switch ( read() ) {
				case ICharacterScanner.EOF:
					break loop;

				case '-': // -  -->
					switch ( read() ) {
						case ICharacterScanner.EOF:
							break loop;

						case '-': // --  -->
							switch ( read() ) {
								case ICharacterScanner.EOF:
								case '>':
									break loop;
							}

							unread();
							break loop;
					}
			}
		}

		return getToken( isInternal() ? DTD_INTERNAL_COMMENT : XML_COMMENT );
	}

	private IToken nextPIToken() {
		state &= STATE_INTERNAL;

		loop: while ( true ) {
			switch ( read() ) {
				case ICharacterScanner.EOF:
					break loop;

				case '?': // ?  ?>
					switch ( read() ) {
						case ICharacterScanner.EOF:
						case '>':
							break loop;
					}

					unread();
			}
		}

		return getToken( isInternal() ? DTD_INTERNAL_PI : XML_PI );
	}

	private IToken nextCDATAToken() {
		state = STATE_DEFAULT;

		loop: while ( true ) {
			switch ( read() ) {
				case ICharacterScanner.EOF:
					break loop;

				case ']': // ]  ]]>
					switch ( read() ) {
						case ICharacterScanner.EOF:
							break loop;

						case ']': // ]]  ]]>
							switch ( read() ) {
								case ICharacterScanner.EOF:
								case '>': // ]]>
									break loop;
							}

							unread();
							unread();
							continue loop;
					}
			}
		}

		return getToken( XML_CDATA );
	}

	private IToken nextConditionalToken() {
		state = STATE_DEFAULT;

		int level = 1;

		loop: while ( true ) {
			switch ( read() ) {
				case ICharacterScanner.EOF:
					break loop;

				case '<': // -  -->
					switch ( read() ) {
						case ICharacterScanner.EOF:
							break loop;

						case '!': // --  -->
							switch ( read() ) {
								case ICharacterScanner.EOF:
									break loop;

								case '[':
									++level;
									continue loop;
							}

							unread();
							continue loop;
					}

					unread();
					continue loop;

				case ']': // -  -->
					switch ( read() ) {
						case ICharacterScanner.EOF:
							break loop;

						case ']': // --  -->
							switch ( read() ) {
								case ICharacterScanner.EOF:
								case '>':
									if ( --level == 0 ) {
										break loop;
									}

									continue loop;
							}

							unread();
							unread();
							continue loop;
					}
			}
		}

		return getToken( DTD_CONDITIONAL );
	}

	private IToken getToken( String type ) {
		length = position - offset;

		if ( length == 0 ) {
			return Token.EOF;
		}

		if ( type == null ) {
			return Token.UNDEFINED;
		}

		IToken token = (IToken) tokens.get( type );
		if ( token == null ) {
			token = new Token( type );
			tokens.put( type, token );
		}

		return token;
	}

	private boolean isInternal() {
		return (state & STATE_INTERNAL) != 0;
	}

	private int read() {
		if ( position >= end ) {
			return ICharacterScanner.EOF;
		}

		try {
			return document.getChar( position++ );
		} catch ( BadLocationException e ) {
			--position;
			return ICharacterScanner.EOF;
		}
	}

	private void unread() {
		--position;
	}

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#getTokenOffset()
	 */
	public int getTokenOffset() {
		return offset;
	}

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#getTokenLength()
	 */
	public int getTokenLength() {
		return length;
	}

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#setRange(IDocument, int, int)
	 */
	public void setRange( IDocument document, int offset, int length ) {
		this.document = document;
		this.begin = offset;
		this.end = offset + length;

		this.offset = offset;
		this.position = offset;
		this.length = 0;
	}

	/*
	 * @see org.eclipse.jface.text.rules.IPartitionTokenScanner
	 */
	public void setPartialRange(
		IDocument document, int offset, int length,
		String contentType, int partitionOffset
	) {
		if ( contentType == XML_ATTRIBUTE ) {
			state = STATE_TAG;
		} else if ( contentType == XML_TAG ) {
			state = isContinuationPartition( document, offset )
				? STATE_TAG : STATE_DEFAULT;
		} else if ( contentType == XML_DECL ) {
			state = isContinuationPartition( document, offset )
				? STATE_DECL : STATE_DEFAULT;
		} else if ( contentType == DTD_INTERNAL ||
				contentType == DTD_INTERNAL_PI ||
				contentType == DTD_INTERNAL_DECL ||
				contentType == DTD_INTERNAL_COMMENT ) {
			state = STATE_INTERNAL;
		} else {
			state = STATE_DEFAULT;
		}

		setRange( document, partitionOffset, length );
	}

	private boolean isContinuationPartition( IDocument document, int offset ) {
		try {
			String type = document.getContentType( offset-1 );

			if ( type != IDocument.DEFAULT_CONTENT_TYPE ) {
				return true;
			}
		} catch ( BadLocationException e ) {}

		return false;
	}
}
