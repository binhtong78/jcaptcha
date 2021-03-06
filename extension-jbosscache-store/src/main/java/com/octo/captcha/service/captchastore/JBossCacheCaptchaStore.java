/*
 * JCaptcha, the open source java framework for captcha definition and integration
 * Copyright (c)  2007 jcaptcha.net. All Rights Reserved.
 * See the LICENSE.txt file distributed with this package.
 */

package com.octo.captcha.service.captchastore;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheException;
import org.jboss.cache.CacheFactory;
import org.jboss.cache.DefaultCacheFactory;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;

import com.octo.captcha.Captcha;
import com.octo.captcha.service.CaptchaServiceException;

/**
 * JBossCache 3.2 implementation of the captcha store. Needs JDK 5.0
 * @see http://community.jboss.org/wiki/JBossCacheOfficialDocumentation
 * @author <a href="mailto:antoine.veret@gmail.com">Antoine Véret</a>
 * @version 2.0
 */
public class JBossCacheCaptchaStore implements CaptchaStore {

	public static final String JCAPTCHA_JBOSSCACHE_CONFIG = "jcaptcha.jbosscache.config";
    private static final String DEFAULT_CACHE_NAME = "/captcha";
    private Fqn<String> cacheQualifiedName;
    private Cache<String, CaptchaAndLocale> cache;

    public JBossCacheCaptchaStore() {
        this(DEFAULT_CACHE_NAME);
    }
    
    public JBossCacheCaptchaStore(String cacheQualifiedName) {
        this.cacheQualifiedName = Fqn.fromString(cacheQualifiedName);
    }

    public boolean hasCaptcha(String s) {

    	try {
    		CaptchaAndLocale result = cache.get(cacheQualifiedName, s);
            if (result != null) {
                return true;
            }
            else
                return false;

        } catch (CacheException e) {
            throw new CaptchaServiceException(e);
        }        
    }

    public void storeCaptcha(String s, Captcha captcha) throws CaptchaServiceException {

        try {
            cache.put(cacheQualifiedName, s, new CaptchaAndLocale(captcha));
        } catch (CacheException e) {
            throw new CaptchaServiceException(e);
        }
    }

    public void storeCaptcha(String s, Captcha captcha, Locale locale) throws CaptchaServiceException {

        try {
            cache.put(cacheQualifiedName, s, new CaptchaAndLocale(captcha, locale));
        } catch (CacheException e) {
            throw new CaptchaServiceException(e);
        }
    }

    public boolean removeCaptcha(String s) {
        try {
        	CaptchaAndLocale captcha = cache.remove(cacheQualifiedName, s);
            if (captcha != null)
                return true;
            else
                return false;
        } catch (CacheException e) {
            throw new CaptchaServiceException(e);
        }
    }

    public Captcha getCaptcha(String s) throws CaptchaServiceException {

        try {
        	CaptchaAndLocale result = cache.get(cacheQualifiedName, s);
            if (result != null) {
                return result.getCaptcha();
            }
            else
                return null;

        } catch (CacheException e) {
            throw new CaptchaServiceException(e);
        }
    }

    public Locale getLocale(String s) throws CaptchaServiceException {

        try {
        	CaptchaAndLocale result = cache.get(cacheQualifiedName, s);
            if (result != null) {
                return result.getLocale();
            }
            else
                return null;

        } catch (CacheException e) {
            throw new CaptchaServiceException(e);
        }
    }

    public int getSize() {

    	try {
    		Node<String, CaptchaAndLocale> root = cache.getRoot();
    		if (root != null) {
    			Node<String, CaptchaAndLocale> captchas = root.getChild(cacheQualifiedName);
    			if (captchas != null)
    				return captchas.dataSize();
    		}
    		return 0;
    	} catch (CacheException e) {
            throw new CaptchaServiceException(e);
        }	
    }

    public Collection<String> getKeys() {

    	try {
    		Node<String, CaptchaAndLocale> root = cache.getRoot();
    		if (root != null) {
    			Node<String, CaptchaAndLocale> captchas = root.getChild(cacheQualifiedName);
    			if (captchas != null) {
    				Collection<String> keys = captchas.getKeys(); 
    		        if (keys != null)
    		        	return keys;
    			}
    		}
    		return Collections.emptySet();
    	} catch (CacheException e) {
            throw new CaptchaServiceException(e);
        }
    }

    public void empty() {
        try {
        	Node<String, CaptchaAndLocale> root = cache.getRoot();
    		if (root != null) {
    			Node<String, CaptchaAndLocale> captchas = root.getChild(cacheQualifiedName);
    			if (captchas != null) {
    				captchas.clearData();
    			}
    		}        	
            cache.removeNode(cacheQualifiedName);
        } catch (CacheException e) {
            throw new CaptchaServiceException(e);
        }
    }
    
    /* (non-Javadoc)
	 * @see com.octo.captcha.service.captchastore.CaptchaStore#initAndStart()
	 */
	public void initAndStart() {
		
		String configFileName = System.getProperty(JCAPTCHA_JBOSSCACHE_CONFIG);
        if (configFileName == null)
            throw new RuntimeException("The system property " + JCAPTCHA_JBOSSCACHE_CONFIG + " have to be set");
		
		CacheFactory<String, CaptchaAndLocale> factory = new DefaultCacheFactory<String, CaptchaAndLocale>();
	    cache = factory.createCache(configFileName);								
	}

	/* (non-Javadoc)
	 * @see com.octo.captcha.service.captchastore.CaptchaStore#shutdownAndClean()
	 */
	public void cleanAndShutdown() {
		cache.stop();
		cache.destroy();
	}
}
