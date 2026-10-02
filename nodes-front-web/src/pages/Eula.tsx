import React from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Navbar from "../components/Navbar";
import { APP_CONFIG, ROUTES, EULA_CONFIG } from "../config/constants";
import styles from "./Eula.module.css";

export const Eula: React.FC = () => {
  const { currentUser } = useAuth();
  const navigate = useNavigate();

  const scrollToSection = (id: string) => {
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: "smooth" });
    }
  };

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handlePortalRedirect = () => {
    if (currentUser) {
      navigate(ROUTES.HOME);
    } else {
      navigate(ROUTES.LOGIN);
    }
  };

  return (
    <div className={styles.eulaPage}>
      {/* Barra superior de navegación unificada */}
      <Navbar />

      <main className={styles.mainContent}>
        <div className={styles.contentContainer}>
          {/* Cabecera principal */}
          <section className={styles.headerCard}>
            <div className={styles.headerGlow}></div>
            <div className={styles.headerTop}>
              <span className={styles.badgeCategory}>{EULA_CONFIG.badgeCategory}</span>
              <span className={styles.badgeDate}>
                Última actualización: {EULA_CONFIG.lastUpdated} • Versión {EULA_CONFIG.version}
              </span>
            </div>
            <h1 className={styles.eulaTitle}>{EULA_CONFIG.title}</h1>
            <p className={styles.eulaSubtitle}>{EULA_CONFIG.subtitle}</p>
          </section>

          {/* Índice de navegación rápida */}
          <nav className={styles.tableOfContents} aria-label="Índice de secciones">
            <div className={styles.tocTitle}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="8" y1="6" x2="21" y2="6" />
                <line x1="8" y1="12" x2="21" y2="12" />
                <line x1="8" y1="18" x2="21" y2="18" />
                <line x1="3" y1="6" x2="3.01" y2="6" />
                <line x1="3" y1="12" x2="3.01" y2="12" />
                <line x1="3" y1="18" x2="3.01" y2="18" />
              </svg>
              <span>Índice del Documento</span>
            </div>
            <div className={styles.tocGrid}>
              <button type="button" onClick={() => scrollToSection("sec-1")} className={styles.tocChip}>
                <span className={styles.tocChipNum}>1</span>
                <span>Descripción del Servicio</span>
              </button>
              <button type="button" onClick={() => scrollToSection("sec-2")} className={styles.tocChip}>
                <span className={styles.tocChipNum}>2</span>
                <span>Contenido (UGC)</span>
              </button>
              <button type="button" onClick={() => scrollToSection("sec-3")} className={styles.tocChip}>
                <span className={styles.tocChipNum}>3</span>
                <span>Moderación y Seguridad</span>
              </button>
              <button type="button" onClick={() => scrollToSection("sec-4")} className={styles.tocChip}>
                <span className={styles.tocChipNum}>4</span>
                <span>Privacidad y Datos</span>
              </button>
              <button type="button" onClick={() => scrollToSection("sec-5")} className={styles.tocChip}>
                <span className={styles.tocChipNum}>5</span>
                <span>Licencia de Uso</span>
              </button>
              <button type="button" onClick={() => scrollToSection("sec-6")} className={styles.tocChip}>
                <span className={styles.tocChipNum}>6</span>
                <span>Limitación de Responsabilidad</span>
              </button>
              <button type="button" onClick={() => scrollToSection("sec-7")} className={styles.tocChip}>
                <span className={styles.tocChipNum}>7</span>
                <span>Eliminación de Cuenta</span>
              </button>
              <button type="button" onClick={() => scrollToSection("sec-8")} className={styles.tocChip}>
                <span className={styles.tocChipNum}>8</span>
                <span>Modificaciones</span>
              </button>
            </div>
          </nav>

          {/* Lista de secciones legales */}
          <div className={styles.sectionsList}>
            {/* 1. DESCRIPCIÓN DEL SERVICIO */}
            <article className={styles.sectionCard} id="sec-1">
              <div className={styles.sectionHeader}>
                <div className={styles.sectionNumber}>1</div>
                <h2 className={styles.sectionTitle}>DESCRIPCIÓN DEL SERVICIO</h2>
              </div>
              <div className={styles.sectionBody}>
                <p className={styles.sectionText}>
                  NODES es una plataforma de colaboración que permite a los usuarios crear espacios de trabajo
                  (Workspaces), intercambiar mensajes, compartir imágenes, gestionar listas de tareas y compartir
                  ubicaciones geográficas en tiempo real.
                </p>
                <div className={styles.featuresPills}>
                  <span className={styles.featurePill}>
                    <svg className={styles.featureIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <polygon points="12 2 2 7 12 12 22 7 12 2" />
                      <polyline points="2 17 12 22 22 17" />
                      <polyline points="2 12 12 17 22 12" />
                    </svg>
                    Workspaces colaborativos
                  </span>
                  <span className={styles.featurePill}>
                    <svg className={styles.featureIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                    </svg>
                    Mensajería e imágenes
                  </span>
                  <span className={styles.featurePill}>
                    <svg className={styles.featureIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <polyline points="9 11 12 14 22 4" />
                      <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
                    </svg>
                    Listas de tareas sincronizadas
                  </span>
                  <span className={styles.featurePill}>
                    <svg className={styles.featureIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M12 2a8 8 0 0 0-8 8c0 5.25 8 12 8 12s8-6.75 8-12a8 8 0 0 0-8-8z" />
                      <circle cx="12" cy="10" r="3" />
                    </svg>
                    Ubicaciones en tiempo real
                  </span>
                </div>
              </div>
            </article>

            {/* 2. CONTENIDO GENERADO POR EL USUARIO (UGC) */}
            <article className={styles.sectionCard} id="sec-2">
              <div className={styles.sectionHeader}>
                <div className={styles.sectionNumber}>2</div>
                <h2 className={styles.sectionTitle}>CONTENIDO GENERADO POR EL USUARIO (UGC)</h2>
              </div>
              <div className={styles.sectionBody}>
                <p className={styles.sectionText}>
                  NODES permite a los usuarios subir y compartir contenido. Usted es el único responsable del contenido
                  que publique.
                </p>

                <div className={styles.zeroToleranceBanner}>
                  <svg className={styles.zeroToleranceIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="12" cy="12" r="10" />
                    <line x1="4.93" y1="4.93" x2="19.07" y2="19.07" />
                  </svg>
                  <div>
                    <div className={styles.zeroToleranceTitle}>Política de Tolerancia Cero</div>
                    <div className={styles.zeroToleranceText}>
                      Queda estrictamente prohibido subir, compartir o transmitir contenido que sea:
                    </div>
                  </div>
                </div>

                <div className={styles.ugcProhibitionsGrid}>
                  <div className={styles.prohibitionCard}>
                    <div className={styles.prohibitionHeader}>
                      <span className={styles.prohibitionBullet}></span>
                      <span className={styles.prohibitionTitle}>Ilegal</span>
                    </div>
                    <p className={styles.prohibitionDesc}>
                      Contenido que promueva actividades delictivas o viole las leyes locales o internacionales.
                    </p>
                  </div>

                  <div className={styles.prohibitionCard}>
                    <div className={styles.prohibitionHeader}>
                      <span className={styles.prohibitionBullet}></span>
                      <span className={styles.prohibitionTitle}>Abusivo u Hostigador</span>
                    </div>
                    <p className={styles.prohibitionDesc}>
                      Insultos, amenazas, acoso o discurso de odio contra individuos o grupos.
                    </p>
                  </div>

                  <div className={styles.prohibitionCard}>
                    <div className={styles.prohibitionHeader}>
                      <span className={styles.prohibitionBullet}></span>
                      <span className={styles.prohibitionTitle}>Obsceno</span>
                    </div>
                    <p className={styles.prohibitionDesc}>
                      Material pornográfico, sexualmente explícito o excesivamente violento.
                    </p>
                  </div>

                  <div className={styles.prohibitionCard}>
                    <div className={styles.prohibitionHeader}>
                      <span className={styles.prohibitionBullet}></span>
                      <span className={styles.prohibitionTitle}>Infractor</span>
                    </div>
                    <p className={styles.prohibitionDesc}>
                      Contenido que viole derechos de autor, marcas registradas o la privacidad de terceros.
                    </p>
                  </div>
                </div>
              </div>
            </article>

            {/* 3. MODERACIÓN Y SEGURIDAD */}
            <article className={styles.sectionCard} id="sec-3">
              <div className={styles.sectionHeader}>
                <div className={styles.sectionNumber}>3</div>
                <h2 className={styles.sectionTitle}>MODERACIÓN Y SEGURIDAD</h2>
              </div>
              <div className={styles.sectionBody}>
                <p className={styles.sectionText}>
                  Para cumplir con las normativas de seguridad de las tiendas de aplicaciones:
                </p>

                <div className={styles.moderationList}>
                  <div className={styles.moderationItem}>
                    <div className={styles.moderationIconWrap}>
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z" />
                        <line x1="4" y1="22" x2="4" y2="15" />
                      </svg>
                    </div>
                    <div className={styles.moderationContent}>
                      <h4>Denuncias</h4>
                      <p>
                        NODES proporciona herramientas para que los usuarios denuncien contenido u otros usuarios que
                        violen estos términos.
                      </p>
                    </div>
                  </div>

                  <div className={styles.moderationItem}>
                    <div className={styles.moderationIconWrap}>
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <circle cx="12" cy="12" r="10" />
                        <line x1="4.93" y1="4.93" x2="19.07" y2="19.07" />
                      </svg>
                    </div>
                    <div className={styles.moderationContent}>
                      <h4>Bloqueo</h4>
                      <p>
                        Los usuarios tienen la capacidad de bloquear a otros usuarios abusivos para dejar de ver sus
                        interacciones.
                      </p>
                    </div>
                  </div>

                  <div className={styles.moderationItem}>
                    <div className={styles.moderationIconWrap}>
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                      </svg>
                    </div>
                    <div className={styles.moderationContent}>
                      <h4>Acción del Desarrollador</h4>
                      <p>
                        Nos reservamos el derecho de eliminar contenido y suspender o eliminar permanentemente las
                        cuentas de usuarios que violen estas políticas, sin previo aviso.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </article>

            {/* 4. PRIVACIDAD Y DATOS */}
            <article className={styles.sectionCard} id="sec-4">
              <div className={styles.sectionHeader}>
                <div className={styles.sectionNumber}>4</div>
                <h2 className={styles.sectionTitle}>PRIVACIDAD Y DATOS</h2>
              </div>
              <div className={styles.sectionBody}>
                <p className={styles.sectionText}>
                  El uso de la Aplicación está sujeto a nuestra Política de Privacidad. La Aplicación utiliza servicios
                  de Google Firebase para la autenticación, almacenamiento de datos y notificaciones. Al usar NODES,
                  usted acepta el procesamiento de sus datos según se describe en nuestra política de seguridad de datos.
                </p>
                <div className={styles.techBadgeRow}>
                  <span className={styles.techBadge}>Firebase Authentication</span>
                  <span className={styles.techBadge}>Cloud Firestore</span>
                  <span className={styles.techBadge}>Cloud Storage</span>
                  <span className={styles.techBadge}>Firebase Cloud Messaging</span>
                </div>
              </div>
            </article>

            {/* 5. LICENCIA DE USO */}
            <article className={styles.sectionCard} id="sec-5">
              <div className={styles.sectionHeader}>
                <div className={styles.sectionNumber}>5</div>
                <h2 className={styles.sectionTitle}>LICENCIA DE USO</h2>
              </div>
              <div className={styles.sectionBody}>
                <p className={styles.sectionText}>
                  Se le otorga una licencia limitada, no exclusiva, no transferible y revocable para utilizar la
                  Aplicación en dispositivos móviles de su propiedad para fines personales o profesionales de
                  colaboración.
                </p>
              </div>
            </article>

            {/* 6. LIMITACIÓN DE RESPONSABILIDAD */}
            <article className={styles.sectionCard} id="sec-6">
              <div className={styles.sectionHeader}>
                <div className={styles.sectionNumber}>6</div>
                <h2 className={styles.sectionTitle}>LIMITACIÓN DE RESPONSABILIDAD</h2>
              </div>
              <div className={styles.sectionBody}>
                <p className={styles.sectionText}>
                  La Aplicación se proporciona "tal cual". El desarrollador no garantiza que el servicio sea
                  ininterrumpido o libre de errores. En ningún caso el desarrollador será responsable por la pérdida de
                  datos, interrupción del negocio o daños resultantes del uso o la imposibilidad de uso de la Aplicación.
                </p>
              </div>
            </article>

            {/* 7. ELIMINACIÓN DE CUENTA */}
            <article className={styles.sectionCard} id="sec-7">
              <div className={styles.sectionHeader}>
                <div className={styles.sectionNumber}>7</div>
                <h2 className={styles.sectionTitle}>ELIMINACIÓN DE CUENTA</h2>
              </div>
              <div className={styles.sectionBody}>
                <p className={styles.sectionText}>
                  Usted puede solicitar la eliminación de su cuenta y todos sus datos asociados en cualquier momento
                  desde el menú de Configuración dentro de la Aplicación o a través de nuestro portal web de soporte.
                  Esta acción es irreversible.
                </p>

                <div className={styles.accountDeletionCallout}>
                  <div className={styles.deletionCalloutText}>
                    <h4>Gestión de Cuenta y Supresión de Datos</h4>
                    <p>Accede al portal web de soporte para ver tu estado o gestionar la eliminación de tu cuenta.</p>
                  </div>
                  <button
                    type="button"
                    onClick={handlePortalRedirect}
                    className={styles.btnActionPortal}
                    id="btn-goto-portal"
                  >
                    <span>Ir a gestión de cuenta</span>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M5 12h14" />
                      <polyline points="12 5 19 12 12 19" />
                    </svg>
                  </button>
                </div>
              </div>
            </article>

            {/* 8. MODIFICACIONES */}
            <article className={styles.sectionCard} id="sec-8">
              <div className={styles.sectionHeader}>
                <div className={styles.sectionNumber}>8</div>
                <h2 className={styles.sectionTitle}>MODIFICACIONES</h2>
              </div>
              <div className={styles.sectionBody}>
                <p className={styles.sectionText}>
                  Nos reservamos el derecho de modificar este EULA en cualquier momento. El uso continuado de la
                  Aplicación tras la publicación de cambios constituye la aceptación de los nuevos términos.
                </p>
              </div>
            </article>
          </div>
        </div>
      </main>

      {/* Pie de página */}
      <footer className={styles.eulaFooter}>
        <div className={styles.eulaFooterContent}>
          <div className={styles.footerLinks}>
            <button type="button" onClick={handlePortalRedirect} className={styles.footerLink}>
              Inicio
            </button>
            <button type="button" onClick={() => scrollToSection("sec-1")} className={styles.footerLink}>
              Términos de Servicio
            </button>
            <button type="button" onClick={() => scrollToSection("sec-4")} className={styles.footerLink}>
              Privacidad
            </button>
            <button type="button" onClick={scrollToTop} className={styles.btnBackToTop}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="18 15 12 9 6 15" />
              </svg>
              <span>Volver arriba</span>
            </button>
          </div>
          <p>{APP_CONFIG.getCopyright()}</p>
        </div>
      </footer>
    </div>
  );
};

export default Eula;
