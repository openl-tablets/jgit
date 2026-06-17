/*
 * Copyright (C) 2010, Sasa Zivkov <sasa.zivkov@sap.com> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.nls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class NLSTest {

	/**
	 * Tomcat's leak detector flags any thread-local value whose class was
	 * loaded by the web-application class loader. After a thread runs a git
	 * operation, the value it leaves in NLS's thread-local must be a
	 * {@link Locale} (bootstrap-loaded), never an {@link NLS} instance, so the
	 * web-application class loader is not retained. See Eclipse bug 550529.
	 */
	@Test
	public void testNoNLSValueRetainedOnWorkerThread()
			throws InterruptedException, ExecutionException {
		ExecutorService pool = Executors.newSingleThreadExecutor();
		try {
			Future<Object> retained = pool.submit(() -> {
				NLS.setLocale(Locale.GERMAN);
				GermanTranslatedBundle.get();
				return threadLocalValue();
			});
			Object value = retained.get();
			assertFalse(
					"worker thread must not retain an NLS value in its thread-local",
					value instanceof NLS);
			assertTrue(
					"worker thread must retain only a Locale in its thread-local",
					value instanceof Locale);
		} finally {
			pool.shutdown();
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		}
	}

	/**
	 * Returns the value the calling thread currently holds in NLS's private
	 * thread-local. Reflecting on NLS (an application class) is permitted by the
	 * module system, unlike reflecting on {@link Thread}'s internal maps.
	 */
	private static Object threadLocalValue() throws ReflectiveOperationException {
		Field localField = NLS.class.getDeclaredField("local");
		localField.setAccessible(true);
		ThreadLocal<?> local = (ThreadLocal<?>) localField.get(null);
		return local.get();
	}

	@Test
	public void testNLSLocale() {
		NLS.setLocale(NLS.ROOT_LOCALE);
		GermanTranslatedBundle bundle = GermanTranslatedBundle.get();
		assertEquals(NLS.ROOT_LOCALE, bundle.effectiveLocale());

		NLS.setLocale(Locale.GERMAN);
		bundle = GermanTranslatedBundle.get();
		assertEquals(Locale.GERMAN, bundle.effectiveLocale());
	}

	@Test
	public void testJVMDefaultLocale() {
		Locale.setDefault(NLS.ROOT_LOCALE);
		NLS.useJVMDefaultLocale();
		GermanTranslatedBundle bundle = GermanTranslatedBundle.get();
		assertEquals(NLS.ROOT_LOCALE, bundle.effectiveLocale());

		Locale.setDefault(Locale.GERMAN);
		NLS.useJVMDefaultLocale();
		bundle = GermanTranslatedBundle.get();
		assertEquals(Locale.GERMAN, bundle.effectiveLocale());
	}

	@Test
	public void testThreadTranslationBundleInheritance() throws InterruptedException {

		class T extends Thread {
			GermanTranslatedBundle bundle;
			@Override
			public void run() {
				bundle = GermanTranslatedBundle.get();
			}
		}

		NLS.setLocale(NLS.ROOT_LOCALE);
		GermanTranslatedBundle mainThreadsBundle = GermanTranslatedBundle.get();
		T t = new T();
		t.start();
		t.join();
		assertSame(mainThreadsBundle, t.bundle);

		NLS.setLocale(Locale.GERMAN);
		mainThreadsBundle = GermanTranslatedBundle.get();
		t = new T();
		t.start();
		t.join();
		assertSame(mainThreadsBundle, t.bundle);
	}

	@Test
	public void testParallelThreadsWithDifferentLocales()
			throws InterruptedException, ExecutionException {

		final CyclicBarrier barrier = new CyclicBarrier(2);

		class GetBundle implements Callable<TranslationBundle> {

			private Locale locale;

			GetBundle(Locale locale) {
				this.locale = locale;
			}

			@Override
			public TranslationBundle call() throws Exception {
				NLS.setLocale(locale);
				barrier.await(); // wait for the other thread to set its locale
				return GermanTranslatedBundle.get();
			}
		}

		ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			Future<TranslationBundle> root = pool.submit(new GetBundle(
					NLS.ROOT_LOCALE));
			Future<TranslationBundle> german = pool.submit(new GetBundle(
					Locale.GERMAN));
			assertEquals(NLS.ROOT_LOCALE, root.get().effectiveLocale());
			assertEquals(Locale.GERMAN, german.get().effectiveLocale());
		} finally {
			pool.shutdown();
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		}
	}
}
