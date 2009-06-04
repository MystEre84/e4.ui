package org.eclipse.e4.compatibility;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextFunction;
import org.eclipse.e4.ui.model.application.MContributedPart;
import org.eclipse.e4.ui.services.IServiceConstants;

/**
 *
 */
public class ActivePartLookupFunction extends ContextFunction {

	public ActivePartLookupFunction() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.core.services.context.spi.ContextFunction#compute(org.
	 * eclipse.e4.core.services.context.IEclipseContext, java.lang.Object[])
	 */
	@Override
	public Object compute(IEclipseContext context, Object[] arguments) {
		IEclipseContext childContext = (IEclipseContext) context
				.getLocal(IServiceConstants.ACTIVE_CHILD);
		if (childContext != null) {
			return childContext.get(getClass().getName());
		}
		return context.get(MContributedPart.class.getName());
	}
}