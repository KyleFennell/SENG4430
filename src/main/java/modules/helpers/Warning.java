package modules.helpers;

import com.github.javaparser.Range;

public class Warning<F, S, T extends Range>  {

	public final F cause;
	public final S recommendedFix;
	public final T lineOrigin;

	public Warning(F cause, S recommendedFix, T lineOrigin) {
		this.cause = cause;
		this.recommendedFix = recommendedFix;
		this.lineOrigin = lineOrigin;
	}

	@Override
	public String toString() {
		return lineOrigin.toString();
	}


}
