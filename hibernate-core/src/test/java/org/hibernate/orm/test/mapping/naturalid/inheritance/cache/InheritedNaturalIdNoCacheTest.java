/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.mapping.naturalid.inheritance.cache;

import org.hibernate.WrongClassException;
import org.hibernate.cfg.AvailableSettings;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.NotImplementedYet;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.hibernate.testing.orm.junit.Setting;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@ServiceRegistry( settings = @Setting( name = AvailableSettings.USE_SECOND_LEVEL_CACHE, value = "false" ) )
@DomainModel( annotatedClasses = { MyEntity.class, ExtendedEntity.class } )
@SessionFactory( )
public class InheritedNaturalIdNoCacheTest {

	@BeforeEach
	public void createTestData(SessionFactoryScope scope) {
		scope.inTransaction(
				(session) -> {
					session.persist( new MyEntity( "base" ) );
					session.persist( new ExtendedEntity( "extended", "ext" ) );
				}
		);
	}

	@AfterEach
	public void dropTestData(SessionFactoryScope scope) {
		scope.inTransaction(
				(session) -> session.createQuery( "delete MyEntity" ).executeUpdate()
		);
	}

	@Test
	public void testLoadRoot(SessionFactoryScope scope) {
		scope.inTransaction(
				(session) -> {
					final MyEntity myEntity = session
							.byNaturalId( MyEntity.class )
							.using( "uid", "base" )
							.load();
					assertThat( myEntity, notNullValue() );
				}
		);

		scope.inTransaction(
				(session) -> {
					final MyEntity myEntity = session
							.bySimpleNaturalId( MyEntity.class )
							.load( "base" );
					assertThat( myEntity, notNullValue() );
				}
		);

		scope.inTransaction(
				(session) -> {
					final MyEntity myEntity = session
							.bySimpleNaturalId( MyEntity.class )
							.load( "extended" );
					assertThat( myEntity, notNullValue() );
				}
		);
	}

	@Test
	@NotImplementedYet( reason = "natural-id / wrong-class support", strict = false )
	public void testLoadSubclass(SessionFactoryScope scope) {
		scope.inTransaction(
				(session) -> {
					final ExtendedEntity extendedEntity = session
							.byNaturalId( ExtendedEntity.class )
							.using( "uid", "extended" )
							.load();
					assertThat( extendedEntity, notNullValue() );
				}
		);
		scope.inTransaction(
				(session) -> {
					final ExtendedEntity extendedEntity = session
							.bySimpleNaturalId( ExtendedEntity.class )
							.load( "extended" );
					assertThat( extendedEntity, notNullValue() );
				}
		);

		// finally try to access the root (base) entity as subclass (extended) - WrongClassException
		scope.inTransaction(
				(session) -> {
					try {
						session
								.bySimpleNaturalId( ExtendedEntity.class )
								.load( "base" );
						fail();
					}
					catch (WrongClassException expected) {
						// expected result
					}
				}
		);
	}

}
