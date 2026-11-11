import { Container, Row, Col, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <Container className="text-center my-5 py-5">
      <Row className="justify-content-center">
        <Col md={8} lg={6}>
          <h1 className="display-1 fw-bold text-primary">404</h1>
          <h2 className="mb-3">¡Página no encontrada!</h2>
          <p className="text-muted mb-4">
            Lo sentimos, la página que estás buscando no existe o ha sido movida.
          </p>
          <Button as={Link} to="/" variant="primary" size="lg">
            Volver al Inicio
          </Button>
        </Col>
      </Row>
    </Container>
  );
}