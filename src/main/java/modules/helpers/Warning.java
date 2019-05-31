package modules.helpers;

import com.github.javaparser.Range;

/**
 * Project          :   Software Quality Assignment
 * Class name       :   Warning.java
 * Author(s)        :   John Barr
 * Date Created     :   23/05/19
 * Purpose          :   This class is used to store information about erroneous data found when analysing a file
 */
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
