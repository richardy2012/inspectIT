package rocks.inspectit.ui.rcp.provider;

import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * INterface for all model classes that can provide a {@link CmrRepositoryDefinition} when they are
 * selected.
 *
 * @author Ivan Senic
 *
 */
public interface ICmrRepositoryProvider {

	/**
	 * Gives the {@link CmrRepositoryDefinition}.
	 *
	 * @return Gives the {@link CmrRepositoryDefinition}.
	 */
	CmrRepositoryDefinition getCmrRepositoryDefinition();

}
