package snownee.kiwi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.ezylang.evalex.parser.ParseException;
import com.ezylang.evalex.parser.Token;
import com.ezylang.evalex.parser.Tokenizer;

@Mixin(value = Tokenizer.class, remap = false)
public abstract class TokenizerMixin {

	@Shadow
	private int currentChar;
	@Shadow
	private int currentColumnIndex;

	@Inject(method = "getNextToken", at = @At(value = "INVOKE", target = "Lcom/ezylang/evalex/parser/Tokenizer;skipBlanks()V", shift = At.Shift.AFTER), cancellable = true)
	private void kiwi$parseSingleQuote(CallbackInfoReturnable<Token> cir) throws ParseException {
		if (currentChar == '\'') {
			int tokenStartIndex = currentColumnIndex;
			StringBuilder tokenValue = new StringBuilder();
			// skip starting quote
			consumeChar();
			boolean inQuote = true;
			while (inQuote && currentChar != -1) {
				if (currentChar == '\\') {
					consumeChar();
					tokenValue.append(escapeCharacter(currentChar));
				} else if (currentChar == '\'') {
					inQuote = false;
				} else {
					tokenValue.append((char) currentChar);
				}
				consumeChar();
			}
			if (inQuote) {
				throw new ParseException(
						tokenStartIndex, currentColumnIndex, tokenValue.toString(), "Closing quote not found");
			}
			cir.setReturnValue(new Token(tokenStartIndex, tokenValue.toString(), Token.TokenType.STRING_LITERAL));
		}
	}

	@Shadow
	protected abstract char escapeCharacter(int currentChar);

	@Shadow
	protected abstract void consumeChar();

}
