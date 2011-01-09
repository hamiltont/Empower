package org.turnerha.environment;

/**
 * Continuous environments have some undesirable properties. Mainly, determining
 * metrics such as accuracy, coverage, etc results in tough math if the
 * environments are continuous. There, in order to speed up prototyping, this
 * interface allows an environment to be cut into variously sized pieces
 * 
 * detecting accuracy
 * 
 * @author hamiltont
 * 
 */
// TODO add in stuff for asking about the available resolution. This can be
// reported in terms of the number of lat/lon decimal points
public interface GridBasedEnvironment {

}
